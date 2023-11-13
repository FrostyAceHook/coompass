package net.fabricmc.twooompass;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import net.fabricmc.api.ModInitializer;

public class Twooompass implements ModInitializer {

    public static int bg_col = 0x80000000;
    public static int text_col = 0xFFFFFFFF;
    public static int shadow_col = 0xFF000000;
    public static int[] pos = {0,0};
    public static int[] shadow_pos = {0,1};
    public static boolean coords_on_left = true;
    public static boolean disable_on_f3 = true;
    public static final String help =
        "# Line structure: {name} = {value}\n" +
        "# The possible names (and the corresponding types):\n" +
        "# - bg_col = {colour}\n" +
        "# - text_col = {colour}\n" +
        "# - shadow_col = {colour}\n" +
        "# - pos = [0-100],[0-100]\n" +
        "# - shadow_pos = [left,middle,right],[top,middle,bottom]\n" +
        "# - coords_on_left = [true,false]\n" +
        "# - disable_on_f3 = [true,false]\n" +
        "# Colour is in rgba, with each value from 0-255 and comma-seperated.\n" +
        "# NOTE: minecraft doesn't support transparent text :c (w this method at least).\n" +
        "# A name can appear multiple times, only the last occurence is used.\n" +
        "# If any value is omitted, the default value is used.\n" +
        "# Deleting this file will automatically create the file with the example contents again.\n" +
        "\n" +
        "bg_col = 0,0,0,128\n" +
        "text_col = 255,255,255,255\n" +
        "shadow_col = 0,0,0,255\n" +
        "pos = 0,0\n" +
        "shadow_pos = middle, bottom\n" +
        "coords_on_left = true\n" +
        "disable_on_f3 = true\n";

    private static int parse_col(String s) throws Exception {
        int col[] = new int[4];

        // Split to rgba.
        String[] rgba = s.split(",");
        if (rgba.length != 4)
            throw new Exception();

        // Read the number.
        for (int i=0; i<4; ++i) {
            col[i] = Integer.parseInt(rgba[i].trim());
            if (col[i] < 0 || 255 < col[i])
                throw new Exception();
        }

        // Convert array to int.
        return col[2] | (col[1] << 8) | (col[0] << 16) | (col[3] << 24);
    }

    private static boolean parse_bool(String s) throws Exception {
        if (s.equals("true"))
            return true;
        if (s.equals("false"))
            return false;

        if (s.equals("1"))
            return true;
        if (s.equals("0"))
            return false;

        throw new Exception();
    }

    private static int[] parse_scale_pos(String s) throws Exception {
        String[] xy = s.split(",");
        if (xy.length != 2)
            throw new Exception();

        int x = Integer.parseInt(xy[0].trim());
        if (x < 0 || 100 < x)
            throw new Exception();

        int y = Integer.parseInt(xy[1].trim());
        if (y < 0 || 100 < y)
            throw new Exception();

        return new int[]{x,y};
    }

    private static int[] parse_pos(String s) throws Exception {
        String[] xy = s.split(",");
        if (xy.length != 2)
            throw new Exception();

        xy[0] = xy[0].trim();
        xy[1] = xy[1].trim();

        int x;
        if (xy[0].equals("left"))
            x = -1;
        else if (xy[0].equals("middle"))
            x = 0;
        else if (xy[0].equals("right"))
            x = 1;
        else throw new Exception();

        int y;
        if (xy[1].equals("top"))
            y = -1;
        else if (xy[1].equals("middle"))
            y = 0;
        else if (xy[1].equals("bottom"))
            y = 1;
        else throw new Exception();

        return new int[]{x,y};
    }

    private static String[] parse_line(String line) throws Exception {
        // Remove comment.
        for (int i=0; i<line.length(); ++i) {
            if (line.charAt(i) == '#') {
                line = line.substring(0,i);
                break;
            }
        }
        // Trim space.
        line = line.trim();

        // If empty line.
        if (line.length() == 0)
            return new String[1];

        // Parse as name-value pair.
        String[] name_value = line.split("=");
        if (name_value.length != 2)
            throw new Exception();

        name_value[0] = name_value[0].trim().toLowerCase();
        name_value[1] = name_value[1].trim().toLowerCase();

        return name_value;
    }


	@Override
	public void onInitialize() {
        // Parse config.
        try {
            String mod_folder = new File(Twooompass.class.getProtectionDomain()
            .getCodeSource().getLocation().toURI()).getParentFile().getPath();

            // Open and create the file if necessary.
            String file_path = mod_folder + "/2oompass.config";
            File file = new File(file_path);
            if (file.createNewFile()) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(help);
                }
            }

            // Read the file.
            try (Scanner reader = new Scanner(file)) {
                while (reader.hasNextLine()) {
                    String[] line = parse_line(reader.nextLine());
                    // If empty line.
                    if (line.length == 1)
                        continue;

                    // Get thang.
                    if (line[0].equals("bg_col"))
                        bg_col = parse_col(line[1]);
                    else if (line[0].equals("text_col"))
                        text_col = parse_col(line[1]);
                    else if (line[0].equals("shadow_col"))
                        shadow_col = parse_col(line[1]);
                    else if (line[0].equals("pos"))
                        pos = parse_scale_pos(line[1]);
                    else if (line[0].equals("shadow_pos"))
                        shadow_pos = parse_pos(line[1]);
                    else if (line[0].equals("coords_on_left"))
                        coords_on_left = parse_bool(line[1]);
                    else if (line[0].equals("disable_on_f3"))
                        disable_on_f3 = parse_bool(line[1]);
                    else throw new Exception();
                }
            }
        } catch (final Exception ex) {}
    }
}
