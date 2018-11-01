package stores.com;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Unit test for simple DroneApp.
 */
public class DroneAppTest
{

    @Test
    public void testOneOrder (){
        Order order = OrderFactory.makeOneOrder("WM001","9:00:00","N3E5");
        Order [] orders = {order};
        Scheduler sch = new Scheduler(orders);
        sch.setOrders(orders);
        sch.sweepingScheduler();
        assertTrue(orders[0].isPromoter());
    }

    /**
     * Orders came in same minute. Drone can not finish the second on the time.
     * One order came in the middle of others and can not be promoter
     */
    @Test
    public void testOneOrderInTheMiddleOfOrdersNoExecuted (){
        Order [] orders = {
                OrderFactory.makeOneOrder("WM001", "6:50:00","N8E8"),
                OrderFactory.makeOneOrder("WM002", "6:50:01","N10E10"),
                OrderFactory.makeOneOrder("WM003", "6:50:02","N9E10")
        };
        Scheduler sch = new Scheduler(orders);
        sch.setOrders(orders);
        sch.sweepingScheduler();
        assertTrue(orders[0].isPromoter() &
                orders[2].isPromoter() &
                (!orders[1].isPromoter()));
    }

    /**
     * Orders came in certain interval.
     */
    @Test
    public void testOneOrderInTheMiddleOfOrdersNoExecuted2 (){
        Order [] orders = {
                OrderFactory.makeOneOrder("WM001", "6:50:00","N8E8"),
                OrderFactory.makeOneOrder("WM002", "7:25:01","N10E10"),
                OrderFactory.makeOneOrder("WM003", "7:00:02","N9E10")
        };
        Scheduler sch = new Scheduler(orders);
        sch.setOrders(orders);
        sch.sweepingScheduler();
        assertTrue(orders[0].isPromoter() &
                orders[2].isPromoter() &
                orders[1].isPromoter());
    }

    /*
    On order came last minute and its potential delivery date is beyond the end of
    drone working time.
     */
    @Test
    public void testOneOrderCamLastMinuteNoExecuted (){
        Order order = OrderFactory.makeOneOrder("WM001",
                "21:50:00","N6E6");
        Order [] orders = {order};
        Scheduler sch = new Scheduler(orders);
        sch.setOrders(orders);
        sch.sweepingScheduler();
        assertTrue(!orders[0].isPromoter());
    }
}
