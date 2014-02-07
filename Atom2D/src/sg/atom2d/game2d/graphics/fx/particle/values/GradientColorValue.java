/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom2d.game2d.graphics.fx.particle.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import sg.atom2d.game2d.graphics.fx.particle.ParticleEmitter;
import sg.atom2d.game2d.graphics.fx.particle.ParticleEmitter;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class GradientColorValue extends ParticleValue {
    private static float[] temp = new float[4];
    float[] colors = {1, 1, 1};
    float[] timeline = {0};

    public GradientColorValue() {
        alwaysActive = true;
    }

    public float[] getTimeline() {
        return timeline;
    }

    public void setTimeline(float[] timeline) {
        this.timeline = timeline;
    }

    public float[] getColors() {
        return colors;
    }

    public void setColors(float[] colors) {
        this.colors = colors;
    }

    public float[] getColor(float percent) {
        int startIndex = 0;
        int endIndex = -1;
        float[] timeline = this.timeline;
        int n = timeline.length;
        for (int i = 1; i < n; i++) {
            float t = timeline[i];
            if (t > percent) {
                endIndex = i;
                break;
            }
            startIndex = i;
        }
        float startTime = timeline[startIndex];
        startIndex *= 3;
        float r1 = colors[startIndex];
        float g1 = colors[startIndex + 1];
        float b1 = colors[startIndex + 2];
        if (endIndex == -1) {
            temp[0] = r1;
            temp[1] = g1;
            temp[2] = b1;
            return temp;
        }
        float factor = (percent - startTime) / (timeline[endIndex] - startTime);
        endIndex *= 3;
        temp[0] = r1 + (colors[endIndex] - r1) * factor;
        temp[1] = g1 + (colors[endIndex + 1] - g1) * factor;
        temp[2] = b1 + (colors[endIndex + 2] - b1) * factor;
        return temp;
    }

    public void save(Writer output) throws IOException {
        super.save(output);
        if (!active) {
            return;
        }
        output.write("colorsCount: " + colors.length + "\n");
        for (int i = 0; i < colors.length; i++) {
            output.write("colors" + i + ": " + colors[i] + "\n");
        }
        output.write("timelineCount: " + timeline.length + "\n");
        for (int i = 0; i < timeline.length; i++) {
            output.write("timeline" + i + ": " + timeline[i] + "\n");
        }
    }

    public void load(BufferedReader reader) throws IOException {
        super.load(reader);
        if (!active) {
            return;
        }
        colors = new float[ParticleEmitter.readInt(reader, "colorsCount")];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ParticleEmitter.readFloat(reader, "colors" + i);
        }
        timeline = new float[ParticleEmitter.readInt(reader, "timelineCount")];
        for (int i = 0; i < timeline.length; i++) {
            timeline[i] = ParticleEmitter.readFloat(reader, "timeline" + i);
        }
    }

    public void load(GradientColorValue value) {
        super.load(value);
        colors = new float[value.colors.length];
        System.arraycopy(value.colors, 0, colors, 0, colors.length);
        timeline = new float[value.timeline.length];
        System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
    }
    
}
