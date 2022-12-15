import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IOUtil {

    private static String sql = "select 1 from dual";


    public static String slurp(String filename) {
        //writebytes(sql, "/tmp/sqlbytes");
        byte[] bytes = getbytes(filename);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    //这个slurp版本需要jdk1.7以上
    public static String slurp_jdk17(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            return new String(bytes, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String slurp_jdk16(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = getbytes(filename);
            return new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";


    }

    public static void writebytes(String str, String filename) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            os.write(str.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getbytes(String filename) {
        File file = new File(filename);
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fis.read(bytesArray); //read file into bytes[]
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesArray;
    }

    public static String _slurp(String file) {
        BufferedReader reader = null;
        try {
            FileInputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, "GB18030");
            reader = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }


    public static List<Path> listdirectory(String directory, String extention) {


        List<Path> filePathList = new ArrayList();
        try {
            Files.newDirectoryStream(Paths.get(directory),
                    path -> path.toString().endsWith("."+extention)).forEach(filePath -> filePathList.add(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePathList;
    }

    public static void main(String[] args) {
        //writebytes(sql, "/tmp/sqlwithbytes.bytes");
        List<Path> list = listdirectory("/tmp", "csv");

        for (Path path : list) {
            System.out.println(path.getFileName());
        }

    }

    public static void addTextToFile(String text, String filename) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(filename, true));
            out.write(text);

        } catch (IOException e) {
            // error processing code
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addTextToFile(List<String> text, String filename) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(filename, true));
            for (String s : text) {
                out.write(s);
                out.write("\r\n");
            }
        } catch (IOException e) {
            // error processing code
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
