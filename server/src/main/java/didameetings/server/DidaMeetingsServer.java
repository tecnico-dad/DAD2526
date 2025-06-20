package didameetings.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import didameetings.DidaMeetingsMain;
import didameetings.DidaMeetingsMainServiceGrpc;

public class DidaMeetingsServer {

    static DidaMeetingsServerState server_state;
								    
    /** Server host port. */
    private static int port;
    
    public static void main(String[] args) throws Exception {

	System.out.println(DidaMeetingsServer.class.getSimpleName());
	
	// Print received arguments.
	System.out.printf("Received %d arguments%n", args.length);
	for (int i = 0; i < args.length; i++) {
	    System.out.printf("arg[%d] = %s%n", i, args[i]);
	}
	
	// Check arguments.
	if (args.length < 4) {
	    System.err.println("Argument(s) missing!");
	    System.err.printf("Usage: java %s baseport replica-id scheduler max_participants\n", Server.class.getName());
	    return;
	}

	int base_port  = Integer.valueOf(args[0]);
	int my_id      = Integer.valueOf(args[1]);
	char scheduler = args[2].charAt(0);
	int max        = Integer.valueOf(args[3]);
	
	server_state = new DidaMeetingsServerState(base_port, my_id, scheduler, max);
	
	port = base_port + my_id;

	final BindableService service_impl = new DidaMeetingsMainServiceImpl(server_state);
	final BindableService master_impl  = new DidaMeetingsMasterServiceImpl(server_state);
	final BindableService paxos_impl   = new DidaMeetingsPaxosServiceImpl(server_state);
	
	// Create a new server to listen on port.
	Server server = ServerBuilder.forPort(port).addService(service_impl).addService(master_impl).addService(paxos_impl).build();
	// Start the server.
	server.start();
	// Server threads are running in the background.
	System.out.println("Server started");
	
	// Do not exit the main thread. Wait until server is terminated.
	server.awaitTermination();
    }
}
