package stores.com;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Order implements Comparable<Order>, Serializable {

    public static final String WEST = "W";
    public static final String EAST = "E";
    public static final String NORTH = "N";
    public static final String SOUTH = "S";
    String label;
    int orderIndex;
    LocalDateTime orderTime;
    LocalDateTime departTime;
    LocalDateTime deliveryTime;
    LocalDateTime deadlineTime;
    int x;
    int y;
    String xLabel;
    String yLabel;
    int elapsedTime;
    boolean promoter;
    boolean dectractor;
    boolean neutral;
    boolean visited;
    double penalty;

    public Order(int orderIndex) {
        this.orderIndex = orderIndex;
        this.label = String.format("WM%03d", orderIndex);
    }

    public Order(String orderIndex) {
        this.orderIndex = Integer.parseInt(orderIndex);
        this.label = String.format("WM%s", orderIndex);
    }

    /**
     * @param other
     * @return
     */
    public int compareTo(Order other) {
        LocalDateTime otherDeadline = other.getDeadlineTime();

        if (deadlineTime.isBefore(otherDeadline)) {
            return -1;
        } else if (deadlineTime == otherDeadline) {
            return 0;
        } else {
            return 1;
        }

    }


    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
        this.xLabel = EAST;
        this.yLabel = NORTH;
        if (x < 0) {
            this.xLabel = WEST;
        }
        if (y < 0) {
            this.yLabel = SOUTH;
        }
        elapsedTime = Math.abs(x) + Math.abs(y);
        //Client is expecting the order would be delivered in one hour
        deadlineTime = orderTime.plusHours(1);
    }

    public void setCoords(String x, String xLabel, String y, String yLabel) {
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.x = Integer.parseInt(x);
        this.y = Integer.parseInt(y);
        if (xLabel == WEST) {
            this.x = -this.x;
        }
        if (yLabel == SOUTH) {
            this.y = -this.y;
        }
        elapsedTime = Math.abs(this.x) + Math.abs(this.y);
        //Client is expecting the order would be delivered in one hour
        deadlineTime = orderTime.plusHours(1);
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public String getOrderTimeString() {
        return orderTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public void setOrderTime(String orderTimeStr) {
        ArrayList<String> parts = OrderFactory.getParts(OrderFactory.TIME_REGEX,orderTimeStr);
        ArrayList<Integer> timesInt  = new ArrayList<>();
        parts.stream().forEach(t -> timesInt.add(Integer.parseInt(t)));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderTime = LocalDateTime.of(now.getYear(), now.getMonth(),
                now.getDayOfMonth(), timesInt.get(0), timesInt.get(1), timesInt.get(2));
        this.orderTime = orderTime;
    }


    public LocalDateTime getDepartTime() {
        return departTime;
    }

    public String getDepartTimeString() {
        return departTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public void setDepartTime(LocalDateTime departTime) {
        this.departTime = departTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getDeliveryTimeString() {
        return deliveryTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public boolean isPromoter() {
        if (deliveryTime != null) {
            return deliveryTime.isBefore(getDeadlineTime()) ||
                    deliveryTime.equals(getDeadlineTime());
        }
        return false;
    }

    public boolean isDetractor() {
        if (deliveryTime != null) {
            return deliveryTime.isAfter(orderTime.plusHours(3));
        }
        return false;
    }

    public boolean isNeutral() {
        return (!isPromoter()) && (!isDetractor());
    }

    public void setPenalty() {
        penalty = -((deliveryTime.getHour() - orderTime.getHour()) * 60 +
                (deliveryTime.getMinute() - orderTime.getMinute())) / 60.0;
        if (isPromoter()) {
            penalty = -penalty;
        }
    }

    public double getPenalty() {
        return penalty;
    }

    @Override
    public Order clone() {
        Order order = null;
        try {
            order = (Order) super.clone();
        } catch (CloneNotSupportedException e) {
            order = new Order(0);
            order.setLabel(getLabel());
            order.setVisited(isVisited());
            order.setDepartTime(getDepartTime());
            order.setDeliveryTime(getDeliveryTime());
            order.setOrderTime(getOrderTime());
            order.setElapsedTime(getElapsedTime());
            order.x = this.x;
            order.y = this.y;
            order.xLabel = this.xLabel;
            order.yLabel = this.yLabel;
        }
        return order;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public String getxLabel() {
        return xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }


    @Override
    public String toString() {
        long timeDiff = 0;
        if (getDeliveryTime() != null) {
            timeDiff = (deliveryTime.getHour() - deadlineTime.getHour()) * 60 +
                    (deliveryTime.getMinute() - deadlineTime.getMinute());
        }
        return label + " - " + orderTime + " - " + departTime + " - " + deliveryTime +
                " x= " + x + " " + xLabel + " y=" + y + " " + yLabel +
                " deadlineTime:" + deadlineTime +
                " promoter:" + isPromoter() + " isNeutral:" + isNeutral() +
                " isDetractor:" + isDetractor() + " timeDiff:" + timeDiff + " penalty:" + penalty;
    }
}
