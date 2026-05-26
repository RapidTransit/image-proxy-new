//package com.pss.proxy.image;//package com.pss.proxy.image;
//
//import io.vertx.core.Vertx;
//import io.vertx.core.cli.Argument;
//import io.vertx.core.cli.CLI;
//import io.vertx.core.cli.CommandLine;
//import io.vertx.core.cli.Option;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.http.RequestOptions;
//import io.vertx.core.net.SocketAddress;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//public class CommandRunner {
//
//    private static final List<String> VALID_COMMANDS = List.of("reset-cache");
//
//    public static void main(String[] args) {
//        CLI cli = CLI.create("CommandRunner")
//                .setSummary("Administration functions for ImageProxy")
//                .addOption(new Option()
//                        .setLongName("name")
//                        .setShortName("n")
//                        .setDescription("The templated service file name")
//                        .setRequired(true)
//                )
//                .addArgument(new Argument().setIndex(0).setArgName("action").setDescription("Action to take").setRequired(true));
//        StringBuilder sb = new StringBuilder();
//        cli.usage(sb);
//        CommandLine commandLine = cli.parse(List.of(args));
//
//        if(!commandLine.isValid() || commandLine.isAskingForHelp()){
//            System.out.println(sb);
//        } else {
//            String name = commandLine.getOptionValue("name");
//            Path socket = Paths.get(String.format("/tmp/image-proxy-%s/image-proxy.sock", name));
//            if(Files.exists(socket)){
//                String arg = commandLine.getArgumentValue("action");
//                if(VALID_COMMANDS.contains(arg)) {
//                    SocketAddress socketAddress = SocketAddress.domainSocketAddress(socket.toString());
//                    Vertx vertx = Vertx.vertx();
//                    HttpClient httpClient = vertx.createHttpClient();
//                    httpClient.request(new RequestOptions().setServer(socketAddress).setHost("localhost")
//                            .setURI("/" + arg)).onSuccess(request-> {
//                                request.send()
//                                        .onSuccess(sucess-> System.out.println("Operation successful"))
//                                        .onFailure(t-> {
//                                            System.err.println("Operation failed");
//                                            t.printStackTrace();
//                                        }).andThen(r-> {
//
//                                        });
//                    }).onComplete(r-> {
//                        vertx.close();
//                    });
//                } else {
//                    System.err.printf("Command %s does not exist, valid commands: %s %n", socket, String.join(", ", VALID_COMMANDS));
//                }
//            } else {
//                System.err.printf("Socket: %s does not exist%n", socket);
//            }
//
//        }
//
//    }
//}
