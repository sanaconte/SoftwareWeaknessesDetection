import java.util.ArrayList;
import java.util.List;

public class ProgramTest {

    public void commandInjection(String osCommand, String data){
        /* POTENTIAL FLAW: command injection */
        Process process = Runtime.getRuntime().exec(osCommand + data);
        process.waitFor();
    }
      public void programTest(String userData){
        String tainted = null;
        String query = "select * from student where id=" + tainted.split(":");
        String conn = mysql_connect("localhost", "mysql_user", "mysql_password");
        mysql_select_db("dbname");
        echo("query : "+query+ tainted.length()+ "<br /><br />");
        String res = mysql_query(query+tainted.length());
        List<String> data;
        while (!(data = mysql_fetch_array(res)).isEmpty()){
            print_r(data);
            echo("<br />" + tainted.length() + "<br />");
        }
        mysql_close(conn);
    }
    public void test(){
        int x = 5;
        int y = 1;
        while(x > 1){
            y = x * y;
            x = x - 1;
        }
    }
    public void mysql_close(String conn){
        System.out.println("closing connection ... "+ conn);
    }

    public void echo(String message){
        System.out.println("echo ... ");
        System.out.println(message);
    }

    public void print_r(List<String> data){
        System.out.println("data: "+data);
    }
    public String mysql_query(String query){
        return "Query resutl";
    }

    public List<String> mysql_fetch_array(String res){
        List<String> list = new ArrayList<>();
        list.add(res);
        return list;
    }

    public void mysql_select_db(String dbname){
        System.out.println("mysql_select_db");
    }

    public String mysql_connect(String localhost, String mysql_user, String mysql_password){
        System.out.println("connecting ....");
        return "Successfull connection";
    }
}
