Drone Scheduling Application

These application will try to minimize the NPS (Net Promoter Score)

NPS = % Promoters - % Detractors
NPS = 100* [(# of Orders completed in 1 hours)/(# of Orders) -
(# of Orders completed less than 2 hours)/(# of Orders)

Main idea here is to increase the number of promoters and based the algorithm on this fact.

Order locations are distributed in N X M matrix and Walmart warehouse
is situated at coordinates (0,0). A drone will deliver order  at location (x,y) by
2*|x+y| minutes. Remember Drone will make round trip to deliver the order. The time value |x+y|
the drone must travel will be called elapsed time in our algorithm. The deadline of an order (x,y)
will be current time + elapsed time

We have used Heap data structure (PriorityQueue in Java library) to keep the minimum distance of
elapsed times for orders.

Drone remove the the order with smallest elapsed time from queue and deliver it.
When it comes back to warehouse, it will looks all orders whose deadline is not over and drone
end time (the drone can work until  10PM) is not passed. It will select minimum of those. If it
could not find such an order, then the drone increase time and try to find next order.
It will iterate this until queue empty

Based on my experiments, I was not able to get good numbers  for more than 100 orders.
Therefore, I have enhanced with multi-drone delivery. I have obtained good speedup. Basically,
it is similar algorithm but with multiple drones. It is simulation and not multi-threaded.


The application will generate an random input file for orders with option -g or
it will accept input file. It will store the output into output file specified with -o option.

As said, with option -n, it can work with multiple drones.

java -jar target/store-1.0-SNAPSHOT-jar-with-dependencies.jar
Missing required option: o
usage: Drone Scheduler
 -g,--generate        Generate Random Input File
 -i,--input <arg>     Input File Path
 -n,--ndrones <arg>   Number of Drones
 -o,--output <arg>    Output File Path

