package stores.com;

import org.apache.commons.cli.*;

import java.io.IOException;


/**
 *
 */
public class DroneApp {

    public static void main(String[] args) throws IOException {
        DroneApp dap = new DroneApp();
        dap.runApp(args);
    }

    public CommandLine getOptions(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "Input File Path");
        input.setRequired(false);
        options.addOption(input);

        Option generate = new Option("g", "generate", false, "Generate Random Input File");
        generate.setRequired(false);
        options.addOption(generate);

        Option output = new Option("o", "output", true, "Output File Path");
        output.setRequired(true);
        options.addOption(output);

        Option ndrones = new Option("n", "ndrones", true, "Number of Drones");
        ndrones.setRequired(false);
        options.addOption(ndrones);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Drone Scheduler", options);
            System.exit(1);
        }
        return cmd;

    }

    public void runApp(String[] args) throws IOException {
        CommandLine cmd = getOptions(args);
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String generate = cmd.getOptionValue("generate");
        String ndrones = cmd.getOptionValue("ndrones");

        boolean isGenSet = cmd.hasOption("generate");
        boolean iSndrones = cmd.hasOption("ndrones");

        //generate input file
        if (isGenSet) {
            OrderFactory.makeInputForDron(outputFilePath);
            return;
        }

        Order[] orders;
        if (inputFilePath == null) {
            orders = OrderFactory.generateOrderTimes();
        } else {
            orders = OrderFactory.readFromInputForDron(inputFilePath);
        }
        //run scheduler now
        Scheduler sch = new Scheduler(orders);
        if (iSndrones){
            sch.sweepingSchedulerWithMultipleDrones(Integer.parseInt(ndrones));
        }else {
            sch.sweepingScheduler();
        }
        OrderFactory.writeSimulationResults(outputFilePath, sch.getOrders());
    }


}
