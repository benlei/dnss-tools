package dnss.tools.pak;

import org.ini4j.Config;

public class Main {
    static {
        Config.getGlobal().setEscape(false);
    }

    private static void showManual() {
        System.out.println("Usage: pak [OPTION...] [FILE]...");
        System.out.println("'pak' archives and gunzips files into a single file, and can extract from a pak file.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  pak -cf Resource00.pak foo bar\t# Creates  Resource00.pak from file/directories foo and bar");
        System.out.println("  pak -tvf Resource00.pak\t\t# Lists all files in Resource00.pak verbosely");
        System.out.println("  pak -xf Resource00.pak\t\t# Extracts all files from Resource00.pak");
    }

    public static int main(String[] args) {
        if (args.length < 2) {
            showManual();
            return 1;
        }

        // TODO...
        showManual();
        return 0;
    }
}
