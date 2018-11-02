package stores.com;

import org.apache.commons.lang.SerializationUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import org.javatuples.Pair;

public class Scheduler {

    Order[] orders;

    /**
     *
     */
    public Scheduler() {
        orders = OrderFactory.generateOrderTimes();
    }

    /**
     *
     * @param orders
     */
    public Scheduler(Order[] orders) {
        this.orders = orders;
    }

    /**
     * Schedule order that is sweeping based with multiple drones
     * @param ndrones
     */
    public void sweepingSchedulerWithMultipleDrones(int ndrones) {
        LocalDateTime now = LocalDateTime.now();
        ArrayList<Order> eliminatedOrders = new ArrayList<>();
        ArrayList<Order> batchOrders = new ArrayList<>();
        PriorityBlockingQueue<Order> queue = new PriorityBlockingQueue<>();

        List<Order> oList = Arrays.stream(orders).collect(Collectors.toList());
        oList.stream().forEach(o -> queue.add(o));
        LocalDateTime curTime = queue.peek().getOrderTime();
        LocalDateTime endTime = LocalDateTime.of(now.getYear(), now.getMonth(),
                now.getDayOfMonth(), OrderFactory.END_TIME, 0, 0);
        boolean isOneProcessed = true;
        ArrayList<LocalDateTime> curTimes = new ArrayList<>();
        for (int j=0;j<ndrones;j++){
            curTimes.add(curTime);
        }
        while ((!queue.isEmpty()) && isOneProcessed) {
            isOneProcessed = processMultipleOrders(ndrones,queue,
                    eliminatedOrders,curTimes,endTime);

        }
        eliminateOrdersWithMultipleDrones(ndrones,queue,
                eliminatedOrders,curTimes,endTime);

        //calculate promoters or detractors
        printNPS();
    }

    /**
     *
     * @param eliminatedOrders
     * @param queue
     * @param endTime
     */
    public void processEliminatedOnes (ArrayList<Order> eliminatedOrders,
                                       PriorityBlockingQueue<Order> queue,
                                       LocalDateTime endTime,
                                       LocalDateTime curTime)
    {
        //add eliminated orders to priority queue again
        eliminatedOrders.stream().forEach(o -> queue.add(o));
        //We may still orders in queue because we have ignored orders that passed deadline above
        while (!queue.isEmpty()) {
            Order nextOrder = queue.remove();
            curTime = eliminateOneOrder (nextOrder,endTime,curTime);
        }
    }

    /**
     *
     * @param nextOrder
     * @param endTime
     * @param curTime
     * @return
     */
    public LocalDateTime eliminateOneOrder (Order nextOrder,
                                    LocalDateTime endTime,
                                    LocalDateTime curTime)
    {
        int elapsedTime = 2 * nextOrder.getElapsedTime();
        //Process order if drone can finish it by end time
        if (curTime.plusMinutes(elapsedTime).isBefore(endTime)) {
            curTime = processOrder(nextOrder, curTime);
        }
        return curTime;
    }

    public void sweepingScheduler() {
        LocalDateTime now = LocalDateTime.now();
        ArrayList<Order> eliminatedOrders = new ArrayList<>();
        ArrayList<Order> batchOrders = new ArrayList<>();
        PriorityBlockingQueue<Order> queue = new PriorityBlockingQueue<>();

        List<Order> oList = Arrays.stream(orders).collect(Collectors.toList());
        oList.stream().forEach(o -> queue.add(o));
        LocalDateTime curTime = queue.peek().getOrderTime();
        LocalDateTime endTime = LocalDateTime.of(now.getYear(), now.getMonth(),
                now.getDayOfMonth(), OrderFactory.END_TIME, 0, 0);

        while ((!queue.isEmpty()) && curTime.isBefore(endTime)) {

            Pair<Order, LocalDateTime> pair = pullOneOrder(queue,
                    eliminatedOrders,curTime,endTime);

            Order nextOrder = pair.getValue0();
            curTime = pair.getValue1();
            //If we are able to find one satisfying order,then process it
            if (nextOrder != null) {
                //Process this order now
                curTime = processOrder(nextOrder, curTime);
            }

        }
        processEliminatedOnes(eliminatedOrders,queue,endTime,curTime);

        //calculate promoters or detractors
        printNPS();
    }


    public boolean processMultipleOrders (int ndrones,
                                                    PriorityBlockingQueue<Order> queue,
                                                    ArrayList<Order> eliminatedOrders,
                                                    ArrayList<LocalDateTime> curTimes,
                                                    LocalDateTime endTime){
        boolean isOneProcessed = false;
        for (int j=0;j<ndrones;j++){
            LocalDateTime curTime = curTimes.get(j);
            Pair<Order, LocalDateTime> pair = pullOneOrder(queue,eliminatedOrders,
                    curTime,endTime);

            Order nextOrder = pair.getValue0();
            //If we are able to find one satisfying order,then process it
            if (nextOrder != null) {
                isOneProcessed = true;
                //Process this order now
                curTime = processOrder(nextOrder, curTime);
            }else {
                curTime = pair.getValue1();
            }
            curTimes.set(j,curTime);

        }

        return isOneProcessed;
    }

    public void eliminateOrdersWithMultipleDrones (int ndrones,
                                          PriorityBlockingQueue<Order> queue,
                                          ArrayList<Order> eliminatedOrders,
                                          ArrayList<LocalDateTime> curTimes,
                                          LocalDateTime endTime){
        //add eliminated orders to priority queue again
        eliminatedOrders.stream().forEach(o -> queue.add(o));
        while (!queue.isEmpty()) {
            for (int j = 0; j < ndrones && (!queue.isEmpty()); j++) {
                LocalDateTime curTime = curTimes.get(j);
                Order nextOrder = queue.remove();
                curTime = eliminateOneOrder(nextOrder,endTime,curTime);
                curTimes.set(j, curTime);
            }
        }
    }

    /**
     *
     * @param batchOrders
     * @param eliminatedOrders
     * @param curTime
     * @param endTime
     * @return
     */
    public Pair<Order, LocalDateTime> pullOneOrder (PriorityBlockingQueue<Order> queue,
                              ArrayList<Order> eliminatedOrders,
                              LocalDateTime curTime,
                              LocalDateTime endTime)
    {
        ArrayList<Order> batchOrders = new ArrayList<>();

        //add all orders to batch that are ordered before current time
        //Pick the shortest one from the batch
        try {
            while ((queue.peek() != null) &&
                    (queue.peek().getOrderTime().isBefore(curTime)
                            || queue.peek().getOrderTime().equals(curTime))) {
                Order o = queue.peek();
                batchOrders.add(queue.remove());
            }
        } catch (Exception ex) {
            System.out.println("stop");
        }

        final LocalDateTime baseTime = curTime;
        //Search order whose expected completion time does not pass actual deadline
        //and  does not pass drone stop working time
        Optional<Order> nextOrder = batchOrders.stream().filter(o -> {
                    int elapsedTime = o.getElapsedTime();
                    LocalDateTime delTime = baseTime.plusMinutes(elapsedTime);
                    return (delTime.isBefore(o.getDeadlineTime()) &&
                            delTime.plusMinutes(elapsedTime).isBefore(endTime)); //Drone makes round trip
                }
        )
                .min(Comparator.comparing(Order::getElapsedTime));
        if (!nextOrder.isPresent()) {
            //we could not find any order made before current time.
            // Sweep time by one minute
            curTime = curTime.plusMinutes(1);
            //System.out.println(curTime + "?" + endTime);
            //add eliminated orders to priority queue again
            batchOrders.stream().forEach(o -> eliminatedOrders.add(o));
            Pair<Order, LocalDateTime> pair = new Pair<>(null,curTime);
            return pair;
        }

        //next order
        Order nextOrderElem = nextOrder.get();

        //delete nextOder from batch because we processed it
        batchOrders.remove(nextOrderElem);
        //add batch to priority queue again
        batchOrders.stream().forEach(o -> queue.add(o));

        Pair<Order, LocalDateTime> pair = new Pair<>(nextOrderElem,curTime);
        return pair;
    }

    /**
     * Schedule order that is shortest first
     */
    public void shortestFirstScheduler() {

        List<Order> sortedList = Arrays.stream(orders).sorted().collect(Collectors.toList());
        //Backup whole order list
        List<Order> listCopy = new ArrayList<>();
        sortedList.stream().forEach(o -> listCopy.add((Order) SerializationUtils.clone(o)));

        LocalDateTime curTime = sortedList.get(0).getOrderTime().minusSeconds(1);
        for (Order order : sortedList) {
            curTime = processOrder(order, curTime);
        }

        //calculate promoters or detractors
        printNPS();
    }

    public void printNPS() {
        int totalOrders = orders.length;
        //calculate promoters or detractors
        long promoters = Arrays.stream(orders).filter(order -> order.isPromoter()).count();
        long detractors = Arrays.stream(orders).filter(order -> order.isDetractor()).count();
        long neutrals = Arrays.stream(orders).filter(order -> order.isNeutral()).count();

        System.out.println("promoters=" + promoters + " detractors=" + detractors + " neutrals=" + neutrals);
        double nps = (promoters / (double) totalOrders) - (detractors / (double) totalOrders);
        System.out.println("nps=" + nps);
    }

    /**
     *
     * @param order
     * @param curTime
     * @return
     */
    public static LocalDateTime processOrder(Order order, LocalDateTime curTime) {
        LocalDateTime nextOrderTime = order.getOrderTime();
        if (curTime.isBefore(nextOrderTime)) {
            curTime = nextOrderTime;
        }
        int orderElapsedTime = order.getElapsedTime();
        order.setDeliveryTime(curTime.plusMinutes(orderElapsedTime));
        order.setDepartTime(curTime);
        order.setVisited(true);
        order.setPenalty();
        //System.out.println(order);
        //Drone deliver and return back to warehouse,
        // round-trip,double elapsed time to go to order location
        curTime = curTime.plusMinutes(2 * orderElapsedTime);
        return curTime;
    }

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
        this.orders = orders;
    }

}
