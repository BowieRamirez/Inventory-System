package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple one-off utility to consolidate items.txt into the new format
 * Usage: run this main from IDE or `mvn -q exec:java -Dexec.mainClass=tools.MigrateItemsToConsolidated`
 */
public class MigrateItemsToConsolidated {
    public static void main(String[] args) throws Exception {
        File in = new File("src/database/data/items.txt");
        if (!in.exists()) {
            System.err.println("items.txt not found: " + in.getAbsolutePath());
            return;
        }

        // Read all single-size lines into groups
        Map<Integer, List<String[]>> groups = new LinkedHashMap<>();
        String header = null;
        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                if (ln == 1) { header = line; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                try {
                    int code = Integer.parseInt(parts[0].trim());
                    groups.computeIfAbsent(code, k -> new ArrayList<>()).add(parts);
                } catch (NumberFormatException e) {
                    // skip
                }
            }
        }

        // Write consolidated file to a backup and new file
        File out = new File("src/database/data/items_consolidated.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            if (header != null) bw.write(header + "\n");
            for (Map.Entry<Integer, List<String[]>> e : groups.entrySet()) {
                List<String[]> list = e.getValue();
                if (list.isEmpty()) continue;
                String[] first = list.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(first[0].trim()).append(',');
                sb.append(first[1].trim()).append(',');
                sb.append(first[2].trim()).append(',');
                for (String[] row : list) {
                    String size = row[3].trim();
                    String qty = row[4].trim();
                    sb.append(size).append(',').append(qty).append(',');
                }
                // price from first
                sb.append(first[5].trim());
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
        }

        System.out.println("Consolidated file written to: " + out.getAbsolutePath());
        System.out.println("If OK, rename the file to items.txt to apply.");
    }
}
