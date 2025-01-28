package net.fabricmc.twooompass;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import net.fabricmc.api.ModInitializer;

public class Twooompass implements ModInitializer {

    public static boolean show_coords = true;
    public static boolean show_compass = true;
    public static boolean show_yaw = false;
    public static boolean show_pitch = false;
    public static int bg_col = 0x80000000;
    public static int text_col = 0xFFFFFFFF;
    public static int shadow_col = 0xFF000000;
    public static int[] pos = {0,0};
    public static int[] shadow_pos = {0,1};
    public static boolean coords_on_left = true;
    public static boolean disable_on_f3 = true;
    public static String name_x = "X";
    public static String name_y = "Y";
    public static String name_z = "Z";
    public static String name_yaw = "T";
    public static String name_pitch = "P";
    public static final String help =
        "# Line structure: {name} = {value}\n" +
        "# The possible names (and the corresponding types):\n" +
        "# - show_coords = [true,false]\n" +
        "# - show_compass = [true,false]\n" +
        "# - show_yaw = [true,false]\n" +
        "# - show_pitch = [true,false]\n" +
        "# - bg_col = {colour}\n" +
        "# - text_col = {colour}\n" +
        "# - shadow_col = {colour}\n" +
        "# - pos = [0-100],[0-100]\n" +
        "# - shadow_pos = [left,middle,right],[top,middle,bottom]\n" +
        "# - coords_on_left = [true,false]\n" +
        "# - disable_on_f3 = [true,false]\n" +
        "# - name_x = \"{string}\"\n" +
        "# - name_y = \"{string}\"\n" +
        "# - name_z = \"{string}\"\n" +
        "# - name_yaw = \"{string}\"\n" +
        "# - name_pitch = \"{string}\"\n" +
        "# Colour is in rgba, with each value from 0-255 and comma-seperated.\n" +
        "# NOTE: minecraft doesn't support transparent text :c (w this method at least).\n" +
        "# A name can appear multiple times, only the last occurence is used.\n" +
        "# If any value is omitted, the default value is used.\n" +
        "# Deleting this file will automatically create the file with this default content again.\n" +
        "\n" +
        "show_coords = true\n" +
        "show_compass = true\n" +
        "show_yaw = false\n" +
        "show_pitch = false\n" +
        "bg_col = 0,0,0,128\n" +
        "text_col = 255,255,255,255\n" +
        "shadow_col = 0,0,0,255\n" +
        "pos = 0,0\n" +
        "shadow_pos = middle, bottom\n" +
        "coords_on_left = true\n" +
        "disable_on_f3 = true\n" +
        "name_x = \"X\"\n" +
        "name_y = \"Y\"\n" +
        "name_z = \"Z\"\n" +
        "name_yaw = \"T\"\n" +
        "name_pitch = \"P\"\n" +
    "";


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
        s = s.toLowerCase();

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

    private static String parse_string(String s) throws Exception {
        if (!s.startsWith("\""))
            throw new Exception();
        if (!s.endsWith("\""))
            throw new Exception();
        // not escaped, just book-ended by double-quotes.
        return s.substring(1, s.length() - 1);
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

        xy[0] = xy[0].trim().toLowerCase();
        xy[1] = xy[1].trim().toLowerCase();

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
        name_value[1] = name_value[1].trim();

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
                    if (line[0].equals("show_coords"))
                        show_coords = parse_bool(line[1]);
                    else if (line[0].equals("show_compass"))
                        show_compass = parse_bool(line[1]);
                    else if (line[0].equals("show_yaw"))
                        show_yaw = parse_bool(line[1]);
                    else if (line[0].equals("show_pitch"))
                        show_pitch = parse_bool(line[1]);
                    else if (line[0].equals("bg_col"))
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
                    else if (line[0].equals("name_x"))
                        name_x = parse_string(line[1]);
                    else if (line[0].equals("name_y"))
                        name_y = parse_string(line[1]);
                    else if (line[0].equals("name_z"))
                        name_z = parse_string(line[1]);
                    else if (line[0].equals("name_yaw"))
                        name_yaw = parse_string(line[1]);
                    else if (line[0].equals("name_pitch"))
                        name_pitch = parse_string(line[1]);
                    else throw new Exception();
                }
            }
        } catch (final Exception ex) {}
    }
}
