import java.net.*; 
import java.io.*; 
import java.security.*;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SesionTCP extends Thread {
    Socket s;
    PrintWriter out; 
    BufferedReader in;

    String recibido;
    String comprobacion;
    String ipcliente;
    String email;

    int idSesion;
    boolean comp = true;
    SesionTCP(Socket s) { 
        this.s=s; 
    } // fin constructor

    // Esto son patrones para comprobar que el email y la IP tienen el formato correcto. Esta copiado de internet...
    public static boolean comprobarEmail(String email) {
        String patronEmail = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";    
        Pattern pattern = Pattern.compile(patronEmail);

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean comprobarIP(String ip) {
        String patronIP = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
        Pattern pattern = Pattern.compile(patronIP);

        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private  String byteArrayToHexString(byte[] b){
        String result = "";
        int j;
        for (int i=0; i < b.length; i++)
        {
            j = ( b[i] & 0xff ) + 0x100;    
            result +=Integer.toString(j, 16).substring(1);
        }
        return result;
    } // fin byteArrayToHexString

    private String createPass(String grupo){  
        try
        {
            String pass = grupo + idSesion;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(pass.getBytes());
            pass = byteArrayToHexString(array);
            return pass;
        }
        catch(NoSuchAlgorithmException e){
            System.out.println(e);
            String error = "Error al crear el Hash";
            return error;

        }

    } // fin createPass

    public void run() { 
        System.out.println("Conectado al servidor");
        try{ 
            in = new BufferedReader ( new InputStreamReader(s.getInputStream())); 
            out = new PrintWriter ( new OutputStreamWriter(s.getOutputStream())); 
            recibido = in.readLine();

        }
        catch(IOException e){ 
            return; 
        } // fin try-catch 

        while(!recibido.equals("QUIT")){
            //Comprobacion del HELLO.
            while(comp){
                try {
                    //recibido = in.readLine();
                    System.out.println(recibido);
                    comprobacion = recibido.substring(0,5);
                    if (comprobacion.equals("HELLO")){
                        ipcliente = recibido.substring(6);
                        System.out.println("La IP recibida es "+ ipcliente);
                        if (!comprobarIP(ipcliente)){ //Si el formato de la IP es incorrecto da error
                            System.out.println("400 ERROR EN FORMATO DE LA IP "+ ipcliente);
                            out.println("400 ERROR EN FORMATO DE LA IP "+ ipcliente);
                            out.flush();
                        }
                        else{ //Si es correcto se pasa a comprobar si coincide la IP enviada por el cliente con la que está mandando informacion al servidor
                            byte[] aux = recibido.substring(5).getBytes();
                            InetAddress cliente = InetAddress.getByAddress(aux);
                            boolean coinciden = cliente.equals(s.getInetAddress());
                            if(coinciden){
                                idSesion = (int)Math.floor( Math.random()*(999999-111111)+111111);
                                System.out.println("200 OK "+ idSesion);
                                out.println("200 OK "+ idSesion);
                                out.flush();
                                comp = false;
                            }else{
                                System.out.println("400 LA IP "+s.getInetAddress()+" NO COINCIDE CON "+ ipcliente);
                                out.println("400 LA IP "+s.getInetAddress()+" NO COINCIDE CON "+ ipcliente);
                                out.flush();
                            }
                        }
                    }

                }
                catch(Exception e){
                    System.out.println(e);
                    System.out.println("Esto esta super mal hecho payaso");
                    return;
                }
            }

            // USER
            comp = true;
            while(comp){
                try {
                    recibido = in.readLine();
                    comprobacion = recibido.substring(0,4);
                    if (comprobacion.equals("USER")){
                        email = recibido.substring(6);
                        if(comprobarEmail(email)){
                            System.out.println("200 ESPERANDO PASS");
                            out.println("200 ESPERANDO PASS");
                            out.flush();
                            comp = false;
                        }
                    }
                    System.out.println("400 ERROR EN FORMATO DE CORREO "+ email);
                    out.println("400 ERROR EN FORMATO DE CORREO "+ email);
                    out.flush();
                }
                catch(IOException e){
                    System.out.println(e);
                    return;
                }
            }

            // PASS

            //Grupos A3A, A3B, A3C, B3A,B3B, C3A,C3B,D3A,D3B
            //Hacer hash con los 9 grupos + idSesion y ver si coinciden con el recibido
            comp = true;
            while(comp){
                try {
                    recibido = in.readLine();
                    comprobacion = recibido.substring(0,4);
                    if (comprobacion.equals("PASS")){
                        String hash = recibido.substring(6);
                        String grupo = "error";
                        String A3A = createPass("A3A"+idSesion);
                        String A3B = createPass("A3B"+idSesion);
                        String A3C = createPass("A3C"+idSesion);
                        String B3A = createPass("B3A"+idSesion);
                        String B3B = createPass("B3B"+idSesion);
                        String C3A = createPass("C3A"+idSesion);
                        String C3B = createPass("C3B"+idSesion);
                        String D3A = createPass("D3A"+idSesion);
                        String D3B = createPass("D3B"+idSesion);

                        if (hash.equals(A3A)){
                            grupo = "A3A";             
                        }
                        if (hash.equals(A3B)){
                            grupo = "A3B";             
                        }
                        if (hash.equals(A3C)){
                            grupo = "A3C";             
                        }
                        if (hash.equals(B3A)){
                            grupo = "B3A";             
                        }
                        if (hash.equals(B3B)){
                            grupo = "B3B";             
                        }
                        if (hash.equals(C3A)){
                            grupo = "C3A";             
                        }
                        if (hash.equals(C3B)){
                            grupo = "C3B";             
                        }
                        if (hash.equals(D3A)){
                            grupo = "D3A";             
                        }
                        if (hash.equals(D3B)){
                            grupo = "D3B";             
                        }
                        if (grupo.equals("error")){
                            System.out.println("400 EL HASH NO CORRESPONDE A NINGUN GRUPO");
                            out.println("400 EL HASH NO CORRESPONDE A NINGUN GRUPO");
                            out.flush();
                        }else{
                            System.out.println("200 ESPERANDO DATA");
                            out.println("200 ESPERANDO DATA");
                            out.flush();
                            comp = false;
                        }     
                    }
                }
                catch(IOException e){
                    System.out.println(e);
                    return;
                }
            }

            //DATA
            comp = true;
            while(comp){
                try {
                    recibido = in.readLine();
                    comprobacion = recibido.substring(0,4);
                    if (comprobacion.equals("DATA")){
                        System.out.println("200 INICIADA FASE DE TRANSFERENCIA. FINALICE CON .");
                        out.println("200 INICIADA FASE DE TRANSFERENCIA. FINALICE CON .");
                        out.flush();
                        while (!recibido.equals(".")){
                            //recibir texto
                            recibido = in.readLine();
                            System.out.println(recibido);
                        }
                        System.out.println("200 OK. FIN FASE TRANSFERENCIA. CIERRE CON QUIT");
                        out.println("200 OK. FIN FASE TRANSFERENCIA. CIERRE CON QUIT");
                        out.flush();
                        comp = false;
                    }else{
                        System.out.println("400 ERROR EL SERVIDOR ESTA ESPERANDO EL COMANDO DATA");
                        out.println("400 ERROR EL SERVIDOR ESTA ESPERANDO EL COMANDO DATA");
                        out.flush();
                    }     
                }
                catch(IOException e){
                    System.out.println(e);
                    return;
                }
            }
        }
        try {
            System.out.println(in.readLine());
            out.close();
            in.close(); 
            s.close(); 
        }
        catch(Exception e){
            return ;
        }

    }// fin run 
} // fin clase SesionTCP 
