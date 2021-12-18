package com.assertsecurity.venariwatcher.httpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @Classname HttpServer
 * @Description HTTPServer supply .class file which execute command by Runtime.getRuntime.exec()
 * @Author welkin
 */
public class HttpServer implements Runnable{
    private Server _server;
    private String _httpAddr;
    private int _httpPort;

    public HttpServer(String httpAddr, int port) {
        _server = new Server(port);
        _httpAddr = httpAddr;
        _httpPort = port;
    }

    @Override
    public void run() {
        ServletHandler handler = new ServletHandler();
        _server.setHandler(handler);

        handler.addServletWithMapping(VerifyPayloadServlet.class, "/verify/*");
        try {
            System.out.println(DateTimeUtils.getDateTimeString() + " [HTTP Server]>> Listening on " + _httpAddr + ":" + _httpPort);
            _server.start();
            _server.join();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressWarnings("serial")
    public static class VerifyPayloadServlet extends HttpServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

            String filename = request.getRequestURI().substring(1);
            filename = filename.replace("verify/", "");
            boolean found = false;
            try 
            {
                UUID id = UUID.fromString(filename);
                if (PayloadMapper.Has(id))
                {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain"); 
                    PrintWriter out = response.getWriter(); 
                    out.println(String.format("Found %s",id)); 
                    found = true;
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            if (!found)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println("Not Found"); 
            }
            
        }
    }

}
