package com.Lia.assistant.ui.components

import android.content.Context
import android.graphics.RuntimeShader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.NovaOrbState

private const val ORB_AGSL = """
uniform float2 resolution;
uniform float time;
uniform float amplitude;
uniform float2 tilt;
uniform float3 coreC;
uniform float3 rimC;
uniform float3 haloC;

float hash(float2 p) { return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453); }
float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + float2(1.0, 0.0));
    float c = hash(i + float2(0.0, 1.0));
    float d = hash(i + float2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.0 + float2(17.0, 9.0);
        a *= 0.5;
    }
    return v;
}

half4 main(float2 frag) {
    float2 uv = (frag - 0.5 * resolution) / (0.5 * min(resolution.x, resolution.y));
    float r = 0.58 * (1.0 + 0.14 * amplitude);
    float d = length(uv);

    float haloA = exp(-max(d - r, 0.0) * 4.5) * 0.55;

    float sphereA = 1.0 - smoothstep(r - 0.012, r, d);
    float3 sphereCol = float3(0.0);
    if (d < r) {
        float z = sqrt(max(r * r - d * d, 0.0));
        float3 n = normalize(float3(uv, z));
        float3 L = normalize(float3(-0.45 + tilt.x * 0.8, -0.55 + tilt.y * 0.8, 0.7));
        float diff = max(dot(n, L), 0.0);
        float3 H = normalize(L + float3(0.0, 0.0, 1.0));
        float spec = pow(max(dot(n, H), 0.0), 48.0);
        float fres = pow(1.0 - n.z, 3.0);

        float2 q = n.xy / (n.z + 1.2) * 1.8;
        float ang = time * 0.2;
        q = float2(cos(ang) * q.x - sin(ang) * q.y, sin(ang) * q.x + cos(ang) * q.y);
        float e = fbm(q + time * 0.15);
        e = smoothstep(0.25, 0.85, e);

        float3 base = mix(coreC * 0.35, coreC * 1.2, e);
        sphereCol = base * (0.35 + 0.85 * diff)
            + float3(1.0) * spec * 0.7
            + rimC * fres * 1.1
            + coreC * amplitude * 0.5 * (1.0 - d / r);
    }

    float3 rgb = sphereCol * sphereA + haloC * haloA * (1.0 - sphereA);
    float a = sphereA + haloA * (1.0 - sphereA);
    return half4(float4(clamp(rgb, 0.0, 1.0), clamp(a, 0.0, 1.0)));
}
"""

/** GPU-shaded lit 3D sphere (AGSL, API 33+). Falls back to the Canvas NovaOrb below that. */
@Composable
fun LiaOrb3D(
    state: NovaOrbState,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    amplitude: Float = 0f,
    style: NovaOrbStyle = NovaOrbStyle.NOVA,
    name: String = "Lia",
) {
    if (Build.VERSION.SDK_INT >= 33) {
        LiaOrb3DShader(state, modifier, size, amplitude, name)
    } else {
        NovaOrb(state, modifier, size, style, amplitude, name)
    }
}

@RequiresApi(33)
@Composable
private fun LiaOrb3DShader(state: NovaOrbState, modifier: Modifier, size: Dp, amplitude: Float, name: String) {
    val c = novaColors
    val reduced = LocalNovaReducedMotion.current
    val pal = orbPalette(state, c)
    val core by animateColorAsState(pal.core, tween(500), label = "core")
    val rim by animateColorAsState(pal.rim, tween(500), label = "rim")
    val halo by animateColorAsState(pal.halo, tween(500), label = "halo")

    val shader = remember { RuntimeShader(ORB_AGSL) }
    var time by remember { mutableFloatStateOf(0f) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    val speed = when (state) {
        NovaOrbState.THINKING -> 2.4f; NovaOrbState.CONNECTING -> 0.6f
        NovaOrbState.LISTENING -> 1.0f; NovaOrbState.SPEAKING -> 1.4f; else -> 0.8f
    }

    LaunchedEffect(reduced, speed) {
        if (reduced) return@LaunchedEffect
        var last = 0L
        while (true) {
            androidx.compose.runtime.withFrameNanos { now ->
                if (last != 0L) time += (now - last) / 1_000_000_000f * speed
                last = now
            }
        }
    }

    val ctx = LocalContext.current
    DisposableEffect(reduced) {
        if (reduced) return@DisposableEffect onDispose {}
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val l = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val tx = (-e.values[0] / 9.8f).coerceIn(-1f, 1f)
                val ty = (e.values[1] / 9.8f).coerceIn(-1f, 1f)
                tiltX += (tx - tiltX) * 0.1f // low-pass
                tiltY += (ty - tiltY) * 0.1f
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (acc != null) sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm.unregisterListener(l) }
    }

    val brush = remember(shader) { ShaderBrush(shader) }
    Canvas(modifier.size(size).semantics { contentDescription = "$name status: ${state.name.lowercase()}" }) {
        shader.setFloatUniform("resolution", this.size.width, this.size.height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("amplitude", amplitude.coerceIn(0f, 1f))
        shader.setFloatUniform("tilt", tiltX, tiltY)
        shader.setFloatUniform("coreC", core.red, core.green, core.blue)
        shader.setFloatUniform("rimC", rim.red, rim.green, rim.blue)
        shader.setFloatUniform("haloC", halo.red, halo.green, halo.blue)
        drawRect(brush)
    }
}
