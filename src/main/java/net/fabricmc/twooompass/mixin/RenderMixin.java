package net.fabricmc.twooompass.mixin;

import net.fabricmc.twooompass.Twooompass;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class RenderMixin extends DrawableHelper {
    @Shadow private MinecraftClient client;

    private static int pad = 2;

    private static int compass_w = 20;

    private int x = 0, y = 0, z = 0;
    private float yaw = 0f, pitch = 0f;
    private int dir = 0, dir_last = -1;

    private String yaw_x, yaw_f, yaw_z;

    private void set_vars() {
        // Only get if not null.
        if (client != null && client.player != null) {
            x = MathHelper.floor(client.player.x);
            y = MathHelper.floor(client.player.y);
            z = MathHelper.floor(client.player.z);

            // map yaw to -180..180
            yaw = (client.player.yaw % 360f);
            if (yaw > 180f) yaw -= 360f;
            if (yaw < -180f) yaw += 360f;
            // pitch should be right as-is.
            pitch = client.player.pitch;

            float tmp = yaw;
            if (tmp < 0f) tmp += 360f; // in 0..360.
            tmp = ((tmp + 45f/2f) % 360f); // for rounding reasons.
            dir = (int)(tmp/45f); // [0,7] where 0 = south (+z), 2 = west (-x).
        }

        // Set strings.
        if (dir != dir_last) {
            dir_last = dir;
            switch (dir) {
                case 0:
                    yaw_x = "";
                    yaw_f = "S";
                    yaw_z = "++";
                    break;
                case 1:
                    yaw_x = "-";
                    yaw_f = "SW";
                    yaw_z = "+";
                    break;
                case 2:
                    yaw_x = "--";
                    yaw_f = "W";
                    yaw_z = "";
                    break;
                case 3:
                    yaw_x = "-";
                    yaw_f = "NW";
                    yaw_z = "-";
                    break;
                case 4:
                    yaw_x = "";
                    yaw_f = "N";
                    yaw_z = "--";
                    break;
                case 5:
                    yaw_x = "+";
                    yaw_f = "NE";
                    yaw_z = "-";
                    break;
                case 6:
                    yaw_x = "++";
                    yaw_f = "E";
                    yaw_z = "";
                    break;
                case 7:
                    yaw_x = "+";
                    yaw_f = "SE";
                    yaw_z = "+";
                    break;
                default:
                    yaw_x = "ERROR";
                    yaw_f = "ERROR";
                    yaw_z = "ERROR";
                    break;
            }
        }
    }

    private void render_text(String text, int x, int y) {
        if (Twooompass.shadow_pos[0] != 0 || Twooompass.shadow_pos[1] != 0)
            client.textRenderer.draw(text, x + Twooompass.shadow_pos[0],
                    y + Twooompass.shadow_pos[1], Twooompass.shadow_col);
        client.textRenderer.draw(text, x, y, Twooompass.text_col);
    }

    @Inject(method = "renderHotbar(Lnet/minecraft/client/util/Window;F)V", at = @At(value = "RETURN"))
    private void render_compass(Window window, float tickDelta, CallbackInfo info) {
        // Don't render if in f3.
        if (Twooompass.disable_on_f3 && client.options.debugEnabled)
            return;

        // Too long to type out.
        boolean show_coords = Twooompass.show_coords;
        boolean show_compass = Twooompass.show_compass;
        boolean show_yaw = Twooompass.show_yaw;
        boolean show_pitch = Twooompass.show_pitch;
        if (!show_coords && !show_compass && !show_yaw && !show_pitch)
            return; // headout.


        // Set things.
        set_vars();

        // Set more things.
        int scale = window.getScaleFactor();
        int w = (client.width + scale - 1) / scale;
        int h = (client.height + scale - 1) / scale;


        // Box dimensions.
        int num_items = ((show_coords || show_compass)?3:0) + (show_yaw?1:0) + (show_pitch?1:0);
        int box_w = ((show_coords || show_yaw || show_pitch) ? 52 : 0) + ((show_coords && show_compass) ? 8 + compass_w : 0)
                + ((show_compass && (!show_coords && !show_yaw && !show_pitch)) ? 4 + compass_w : 0);
        int box_h = 9*num_items + (num_items + 2)*pad + ((num_items > 3) ? 2*pad : 0);
        int box_x = (w - box_w) * Twooompass.pos[0] / 100;
        int box_y = (h - box_h) * Twooompass.pos[1] / 100;


        // Compass/coords position.
        int coords_x, compass_x;
        if (Twooompass.coords_on_left) {
            coords_x = box_x + 2 * pad;
            compass_x = box_x + box_w - compass_w;
        } else {
            compass_x = box_x + 2 * pad;
            coords_x = compass_x + compass_w;
        }
        // Move compass left if no coords.
        if (!show_coords)
            compass_x = coords_x;


        // Background.
        fill(box_x, box_y, box_x + box_w, box_y + box_h, Twooompass.bg_col);

        // The full display might look like:
        // X: ***      ++
        // Y: ***      D
        // Z: ***      --
        //
        // T: **.*
        // P: **.*

        int dy = pad; // just lines things up better.

        // Coords + compass.
        dy += pad;
        if (Twooompass.show_coords)
            render_text(Twooompass.name_x+": "+Integer.toString(x), coords_x, box_y + dy);
        if (Twooompass.show_compass)
            render_text(yaw_x, compass_x, box_y + dy);
        if (Twooompass.show_coords || Twooompass.show_compass)
            dy += 9 + pad;
        if (Twooompass.show_coords)
            render_text(Twooompass.name_y+": "+Integer.toString(y), coords_x, box_y + dy);
        if (Twooompass.show_compass)
            render_text(yaw_f, compass_x, box_y + dy);
        if (Twooompass.show_coords || Twooompass.show_compass)
            dy += 9 + pad;
        if (Twooompass.show_coords)
            render_text(Twooompass.name_z+": "+Integer.toString(z), coords_x, box_y + dy);
        if (Twooompass.show_compass)
            render_text(yaw_z, compass_x, box_y + dy);
        if (Twooompass.show_coords || Twooompass.show_compass)
            dy += 9 + pad;

        // Direction.
        if (Twooompass.show_coords || Twooompass.show_compass)
            dy += 2*pad;
        if (Twooompass.show_yaw) {
            String yaw_str = String.format("%.1f", yaw);
            if (yaw_str.equals("-0.0")) yaw_str = "0.0"; // classic.
            render_text(Twooompass.name_yaw+": "+yaw_str, coords_x, box_y + dy);
            dy += 9 + pad;
        }
        if (Twooompass.show_pitch) {
            String pitch_str = String.format("%.1f", pitch);
            if (pitch_str.equals("-0.0")) pitch_str = "0.0";
            render_text(Twooompass.name_pitch+": "+pitch_str, coords_x, box_y + dy);
            dy += 9 + pad;
        }

        // some of the most heinous code ever written ngl.
    }
}
