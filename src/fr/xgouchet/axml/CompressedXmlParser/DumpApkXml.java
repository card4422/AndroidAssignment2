package fr.xgouchet.axml.CompressedXmlParser;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

public class DumpApkXml {
    private static HashMap<String,Integer> peri_freq = new HashMap<String,Integer>();
    private static HashMap<String,Integer> app_freq = new HashMap<String,Integer>();
    private static String name;
    private static int count;
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java fr.xgouchet.axml.CompressedXmlParser.DumpApkXml <file.apk|file.zip> [<path-in-apk>]");
            System.err.println("       java fr.xgouchet.axml.CompressedXmlParser.DumpApkXml <file-containing-entry-from-apk>");
            System.exit(21);
        }

        //
        //File filepath = new File("/apk");
        File filepath = new File(args[0]);
        File[] files = filepath.listFiles();
        //
        String appName;
        //fileName must be changed to several files
        //String fileName = args[0];
        for(File f : files){
            InputStream is = null;
            ZipFile zip = null;

            if (f.getName().endsWith(".apk") || f.getName().endsWith(".zip")) {
                String entryName = args.length > 1? args[1] : "AndroidManifest.xml";
                zip = new ZipFile(f);
                ZipEntry entry = zip.getEntry(entryName);
                is = zip.getInputStream(entry);
                appName=f.getName();
                count = 0;

                //
                try {
                    Document doc = new CompressedXmlParser().parseDOM(is);
                    processNode(doc.getChildNodes().item(0));
                    //dumpNode(doc.getChildNodes().item(0),"");
                }
                catch (Exception e) {
                    System.err.println("Failed AXML decode: " + e);
                    e.printStackTrace();
                }

                is.close();
                if (zip != null) {
                    zip.close();
                }
            } else {
             //   is = new FileInputStream(f);
                continue;
            }

            app_freq.put(appName,count);
        }

        try {
            File writename = new File("permission_freq.txt");
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for(String key : peri_freq.keySet()){
                out.write(key+ ","+peri_freq.get(key)+",\r\n");

            }

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File writename = new File("app_freq.txt");
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for(String key : app_freq.keySet()){
                out.write(key+ ","+app_freq.get(key)+",\r\n");

            }

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



        /*
        for(String key : m.keySet()){
            System.out.println(key+ ","+m.get(key)+",");

        }*/

    }

    private static void processNode(Node node){
        if(node.getNodeName().contains("uses-permission")) {
            for (int i = 0, n = node.getAttributes().getLength(); i < n; i++) {
                Node attr = node.getAttributes().item(i);

                if (attr.getNodeName().equals("android:name")) {
                    count++;
                    if (peri_freq.containsKey(attr.getNodeValue())) {
                        peri_freq.put(attr.getNodeValue(), peri_freq.get(attr.getNodeValue()) + 1);
                    } else {
                        peri_freq.put(attr.getNodeValue(), 1);
                    }
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; ++i)
            processNode(children.item(i));

    }


    private static void dumpNode(Node node, String indent) {
        System.out.println(indent + node.getNodeName() + " " + attrsToString(node.getAttributes()) + " -> " + node.getNodeValue());
        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; ++i)
            dumpNode(children.item(i), indent + "   ");
    }

    private static String attrsToString(NamedNodeMap attrs) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0, n = attrs.getLength(); i < n; ++i) {
            if (i != 0)
                sb.append(", ");
            Node attr = attrs.item(i);
            sb.append(attr.getNodeName() + "=" + attr.getNodeValue());
        }
        sb.append(']');
        return sb.toString();
    }
}