package stores.com;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OrderFactory {
    public static int START_TIME = 6;
    public static int END_TIME = 22;
    public static int MAX_ORDERS = 500;
    public static int MAX_GRID_X = 12;
    public static int MAX_GRID_Y = 12;
    public static String WM_REGEX = "WM(\\d+)";
    public static String COORD_REGEX = "(S|N)(\\d+)(W|E)(\\d++)";
    public static String TIME_REGEX = "(\\d+):(\\d+):(\\d+)";

    public static Order[] generateOrderTimes() {
        return generateOrderTimes(MAX_ORDERS, START_TIME, END_TIME, MAX_GRID_X, MAX_GRID_Y);
    }

    public static Order[] generateOrderTimes(int numberOfOrders,
                                             int startTime, int endTime,
                                             int maxGridX, int maxGridY) {
        LocalDateTime now = LocalDateTime.now();

        int[] hours = new Random().ints(numberOfOrders, startTime, endTime).toArray();
        int[] mins = new Random().ints(numberOfOrders, 0, 60).toArray();
        int[] secs = new Random().ints(numberOfOrders, 0, 60).toArray();

        Random rand = new Random();
        ArrayList<Order> orders = new ArrayList<Order>();
        for (int j = 0; j < numberOfOrders; j++) {
            LocalDateTime orderTime = LocalDateTime.of(now.getYear(), now.getMonth(),
                    now.getDayOfMonth(), hours[j], mins[j], secs[j]);
            // create oder and set order time
            Order order = new Order(j);
            order.setOrderTime(orderTime);

            //set coordinates of order.
            //Think warehouse are sitting in the center (0,0) of coordinate system
            //North is +y, South -y, west -x and east +x
            int horizontalCoor = rand.nextInt(maxGridX / 2) + 1;
            int verticalCoor = rand.nextInt(maxGridY / 2) + 1;
            int x = rand.nextInt(2) == 0 ? horizontalCoor : -horizontalCoor;
            int y = rand.nextInt(2) == 0 ? verticalCoor : -verticalCoor;
            order.setCoords(x, y);
            orders.add(order);
            //System.out.println(order.toString());
        }
        return orders.toArray(new Order[]{});
    }

    public static void makeInputForDron(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        Order[] orders = generateOrderTimes(MAX_ORDERS, START_TIME, END_TIME,
                MAX_GRID_X, MAX_GRID_Y);
        Arrays.stream(orders).forEach(o -> {
            String dim = String.format("%s%d%s%d",o.getyLabel(), Math.abs(o.getY()),
                    o.getxLabel(), Math.abs(o.getX()));
            LocalDateTime ldt = o.getOrderTime();
            String orderTime = String.format("%d:%d:%d", ldt.getHour(), ldt.getMinute(), ldt.getSecond());
            String line = String.format("%s %s %s", o.getLabel(), dim, orderTime);
            //System.out.println(line);
            printWriter.println(line);
        });
        fileWriter.close();
    }

    public static void writeSimulationResults(String fileName, Order[] orders) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        long cnt = Arrays.stream(orders).filter(o -> o.getDepartTime() == null).count();

        Arrays.stream(orders).filter(o->o.getDepartTime() != null).
                sorted(Comparator.comparing(Order::getOrderTime)).forEach(o -> {
            try {
                LocalDateTime ldt = o.getDepartTime();
                LocalTime departTime = LocalTime.of(ldt.getHour(), ldt.getMinute(), ldt.getSecond());
                String departTimeString = String.format("%d:%d:%d", departTime.getHour(),
                        departTime.getMinute(), departTime.getSecond());
                String line = String.format("%s %s", o.getLabel(), departTimeString);
                //System.out.println(line);
                printWriter.println(line);
            } catch (Exception ex) {
                System.out.println("error");
            }
        });
        fileWriter.close();
    }

    public static Order[] readFromInputForDron(String fileName) throws IOException {
        ArrayList<Order> orders = new ArrayList<Order>();
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            for (Object line : stream.toArray()) {
                //read each line, parse it and create a new Order object
                // and add it to ArrayList
                String[] parts = ((String) line).split("\\s");
                //System.out.println(parts[0] + ":" + parts[1] + ":" + parts[2]);

                Order order = makeOneOrder(parts[0],parts[2],parts[1]);
                orders.add(order);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders.toArray(new Order[]{});
    }

    public static Order makeOneOrder (String wmLabel, String timesInput, String coordsInput)
    {

        LocalDateTime now = LocalDateTime.now();
        ArrayList<Integer> timesInt = new ArrayList<>();
        String orderIndex = getParts(WM_REGEX, wmLabel).get(0);
        Order order = new Order(orderIndex);

        //Set times
        ArrayList<String> times = getParts(TIME_REGEX, timesInput);
        times.stream().forEach(t -> timesInt.add(Integer.parseInt(t)));
        LocalDateTime orderTime = LocalDateTime.of(now.getYear(), now.getMonth(),
                now.getDayOfMonth(), timesInt.get(0), timesInt.get(1), timesInt.get(2));
        order.setOrderTime(orderTime);

        //Set coordinates
        ArrayList<String> coords = getParts(COORD_REGEX, coordsInput);
        order.setCoords(coords.get(3), coords.get(2),
                coords.get(1), coords.get(0));
        return order;
    }

    public static ArrayList<String> getParts(String regex, String target) {
        ArrayList<String> res = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        if (matcher.find()) {
            Stream<Integer> infiniteStream = Stream.iterate(1, i -> i + 1);
            infiniteStream
                    .limit(matcher.groupCount()).forEach(n -> {
                res.add(matcher.group(n));
            });
        }
        return res;
    }

}
