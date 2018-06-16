package arithmetic_huffman;

import java.io.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javafx.util.Pair;

/**
 *
 * @author HDrmi
 */
public class Arithmetic_Huffman {

    @SuppressWarnings("FieldMayBeFinal")
    private Map<Character, Pair<Double, Double>> ranges = new HashMap<>();

    private void Listranges(Set<Character> symbolls, double[] probability) {
        double Low = 0.0;
        for (char c : symbolls) {
            ranges.put(c, new Pair<>(Low, (Low + probability[c])));
            Low += probability[c];
        }
    }

    private Pair<Character, Pair<Double, Double>> Getchar(double code) {
        Pair<Character, Pair<Double, Double>> res = null;
        for (Map.Entry<Character, Pair<Double, Double>> ent : ranges.entrySet()) {
            if (code > ent.getValue().getKey() && code < ent.getValue().getValue()) {
                res = new Pair<>(ent.getKey(), new Pair<>(ent.getValue().getKey(), ent.getValue().getValue()));
                break;
            }
        }
        return res;
    }

    private void Write(int length, int NumoberOfChar, double code) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream("Compress.txt"))) {
            out.writeByte(length);
            out.writeByte(NumoberOfChar);
            for (Map.Entry<Character, Pair<Double, Double>> entry : ranges.entrySet()) {
                out.writeByte(entry.getKey());
                out.writeDouble(entry.getValue().getValue() - entry.getValue().getKey());
            }
            out.writeDouble(code);
        } catch (IOException ex) {
            Logger.getLogger(Arithmetic_Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void Write(String str,String filename) {
        try {
            try (FileWriter fw = new FileWriter(new File(filename))) {
                fw.write(str);
                fw.close();
            }

        } catch (IOException iox) {
            //do stuff with exception
        }
    }
    
    private double Code(String text) {
        double low = ranges.get(text.charAt(0)).getKey();
        double high = ranges.get(text.charAt(0)).getValue();
        double tmplow, tmphigh;
        for (int i = 1; i < text.length(); i++) {
            tmplow = low;
            tmphigh = high;
            low = tmplow + (tmphigh - tmplow) * ranges.get(text.charAt(i)).getKey();
            high = tmplow + (tmphigh - tmplow) * ranges.get(text.charAt(i)).getValue();
        }
        return (high + low) / 2;
    }

    public double Compress(String str, int size) {
        String text = str.substring(0, size);
        Write(text,"text.txt");
        double[] probability = new double[255];
        Set<Character> symbolls = new HashSet<>();
        for (char c : str.toCharArray()) {
            probability[c]++;
            symbolls.add(c);
        }
        for (char c : symbolls) {
            probability[c] = probability[c] * 1.0 / str.length();
            System.out.println(probability[c]);
        }
        Listranges(symbolls, probability);
        double returnvalue = Code(text);
        Write(size, symbolls.size(), returnvalue);
        return returnvalue;
    }

    public String Decompress() {
        ranges.clear();
        double code = 0.0;
        int size = 0;
        double[] probability = new double[255];
        Set<Character> symbolls = new HashSet<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream("Compress.txt"))) {
            size = in.readByte() & 255;
            int length = in.readByte() & 255;
            for (int i = 0; i < length; i++) {
                char c = (char) (in.readByte() & 255);
                symbolls.add(c);
                probability[c] = in.readDouble();
            }
            code = in.readDouble();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(Arithmetic_Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
        Listranges(symbolls, probability);
        String res = "";
        Pair<Character, Pair<Double, Double>> tmp = null;
        while (size != 0) {
            tmp = Getchar(code);
            res += tmp.getKey();
            code = (code - tmp.getValue().getKey()) / (tmp.getValue().getValue() - tmp.getValue().getKey());
            size--;
        }
        Write(res,"Decompress.txt");
        return res;
    }


}
