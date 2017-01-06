/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Write a description of class here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import java.net.*;
import java.io.*;
import java.security.*;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

class ClienteTCP {
    Socket s;
    PrintWriter out;
    BufferedReader in;
    String idSession;

    ClienteTCP(){
        String idSession = "";
    } // fin constructor

    private int checkAnswer(String answer)
    {
        int res;
        String comp = answer.substring(0,3);
        if(comp.equals("200")){
            res = 0;
        }
        else{
            res = 1;
        }
        return res;

    } // fin checkAnswer

    private  String byteArrayToHexString(byte[] b)
    {
        String result = "";
        int j;
        for (int i=0; i < b.length; i++)
        {
            j = ( b[i] & 0xff ) + 0x100;    
            result +=Integer.toString(j, 16).substring(1);
        }
        return result;
    } // fin byteArrayToHexString

    private String createPass(String grupo) 
    {  
        try
        {
            ClienteTCP clienteTCP2 = new ClienteTCP();
            String pass = grupo + idSession;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(pass.getBytes());
            pass = clienteTCP2.byteArrayToHexString(array);
            return pass;
        }
        catch(NoSuchAlgorithmException e){
            System.out.println(e);
            String error = "Error al crear el Hash";
            return error;

        }

    } // fin createPass

    public int connect()
    {
        int con;
        try{
            s = new Socket("192.168.1.75",8080);
            in = new BufferedReader (new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            con = 0;

        }
        catch(IOException e){
            System.out.println(e);
            con = 1;
        }
        return con;
    }

    public void disconnect() throws Exception
    {
        out.close();
        in.close();
        s.close();
    }

    public int hello(String ip)
    {
        String answer="";
        System.out.println("HELLO "+ ip);
        out.println("HELLO "+ ip);
        out.flush();

        try{
            answer=in.readLine();
            System.out.println(answer);
        }
        catch (IOException e) {
            System.out.println("ERROR: No llega respuesta de hello");
            System.out.println(e);

        }

        if (checkAnswer(answer)==0)
        {
            idSession = answer.substring(7,13);
            System.out.println("id session = "+idSession);
            return 0;
        }

        return 1;   
    }

    public int user(String email)
    {
        String answer="";
        System.out.println("USER "+ email);
        out.println("USER "+ email);
        out.flush();

        try{
            answer=in.readLine();
            System.out.println(answer);
        }
        catch (IOException e) {
            System.out.println("ERROR: No llega respuesta del user ");
            System.out.println(e);

        }

        if (checkAnswer(answer)==0)
        {
            return 0;
        }

        return 1;

    }

    public int pass(String grupo)
    {
        String answer="";
        String pass =createPass(grupo);
        System.out.println("PASS "+pass);
        out.println("PASS " +pass);
        out.flush();
        try
        {
            answer=in.readLine();
            System.out.println(answer);
        }
        catch (IOException e) 
        {
            System.out.println("ERROR: No llega respuesta del pass");
            System.out.println(e);
        }
        if (checkAnswer(answer)==0)
        {
            return 0;
        }

        return 1;

    }

    public int startData()
    {
        String answer="";
        System.out.println("DATA");
        out.println("DATA");
        out.flush();

        try{
            answer=in.readLine();
            System.out.println(answer);
        }
        catch (IOException e) {
            System.out.println("ERROR: No llega confirmacion de DATA ");
            System.out.println(e);

        }

        if (checkAnswer(answer)==0)
        {
            return 0;
        }

        return 1;
    }

    public void data(String data)
    {
        System.out.println(data);
        out.println(data);
        out.flush();
    }

    public int finData()
    {
        String answer="";
        System.out.println(".");
        out.println(".");
        out.flush();

        try{
            answer=in.readLine();
            System.out.println(answer);
        }
        catch (IOException e) {
            System.out.println("ERROR: No llega confirmacion de final de transferencia ");
            System.out.println(e);

        }

        if (checkAnswer(answer)==0)
        {
            return 0;
        }

        return 1;
    }

    public void quit()
    {
        String answer="";
        System.out.println("QUIT");
        out.println("QUIT");
        out.flush();
    }

    public static void main(String args[]) throws Exception{
        InetAddress ipLocal = InetAddress.getLocalHost();
        String ip = ipLocal.getHostAddress();

        String email = "cardobar@cam.upv.es y cargarr9@teleco.upv.es";
        String grupo = "D3A";

        ClienteTCP clienteTCP = new ClienteTCP();
        
        if (clienteTCP.connect()!=0)
        {
            System.out.println("ERROR: No se puede conectar con el servidor");
            return;
        }

        if (clienteTCP.hello(ip)!=0)
        {
            System.out.println("ERROR: No se puede completar hello");
            return;
        }

        if (clienteTCP.user(email)!=0)
        {
            System.out.println("ERROR: No se puede completar hello");
            return;
        }

        if (clienteTCP.pass(grupo)!=0)
        {
            System.out.println("ERROR: No se puede completar pass");
            return;
        }
        
        if (clienteTCP.startData()!=0)
        {
            System.out.println("ERROR: No se puede completar startData");
            return;
        }

        clienteTCP.data("Domingo Barrero");
        clienteTCP.data("Carlos");
        clienteTCP.data("Garcia Roig");
        clienteTCP.data("Carlos");
        clienteTCP.data(grupo);
        
        if (clienteTCP.finData()!=0)
        {
            System.out.println("ERROR: No se puede completar finData");
            return;
        }

        clienteTCP.quit();
        clienteTCP.disconnect();
    }
}

