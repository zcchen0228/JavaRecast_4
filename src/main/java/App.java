import org.recast4j.detour.FindNearestPolyResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Our crowd simulation class that extends CrowdSimApp.
 * CrowdSimApp is a wrapper around the Java version of Recast/Detour,
 * an open source crowd simulator.
 */
public class App extends CrowdSimApp{


    App() {

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();

            int currentMillisecond = 0; //The current time
            int millisecondsBetweenFrames = 40; //40ms between frames, or 25fps
            int secondsOfSimulation = 20; //How long should the simulation run? Change this to whatever you want.
            for (int i = 0; i < 25*secondsOfSimulation; i++) {

                //Tell the crowd to update itself.
                crowd.update(1 / 25f, null);

                //Write the agents' positions in a file
                writeAgentPosition(writer, currentMillisecond);
                //Update the current simulation time
                currentMillisecond+=millisecondsBetweenFrames;


                //At this point, we've loaded in.csv and store the starting positions/destinations
                //of each agent in a list called agents.
                //We'll loop over the agents and tell the crowd simulator
                //where each agent is and where each agent wants to go
                for(int j = 0; j < agents.size(); j++)
                {
                    Agent agent = agents.get(j); //Grab each agent in the list
                    float[] start = agent.getStart();//Get the agent's starting point as a float array
                    crowd.addAgent(start, ap); //Assign that point to the agent

                    //Now find the nearest valid location to the agent's desired destination
                    //and assign that nearest point.
                    FindNearestPolyResult nearest = query.findNearestPoly(agent.getEnd(), ext, filter);
                    crowd.requestMoveTarget(j, nearest.getNearestRef(), nearest.getNearestPos());


                    /**
                     * 1. set final destination
                     * 2. agents have to walk through a "gate" which takes 3 to 5 secs per agent
                     *    (set obstacle to let agent move through the entry)
                     * 3. set short-term destination to the gate
                     * 4. when agent comes to the entry.( has two condition )
                     *      1. no agent, go to gate directly
                     *      2. agents line up already, go to rear of the queue
                     *
                     */

                    /***
                     * security check:
                     *     1. Each agent must walk through "gate" one by one
                     *     2. Each agent also need to take certain seconds
                     *
                     */

                    float[] gate1 = new float[]{0,0.31f,-1};
                    float[] gate0 = new float[]{0,0.31f,0};
                    float[] trashCan = new float[]{4,0.31f,-6}; // where the trash can is

                    /**
                     * agent 1 go to throw the gate
                     */
                    if (currentMillisecond > 0 && currentMillisecond < 3000) {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(trashCan, ext, filter);
                        crowd.requestMoveTarget(1, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

                    if (currentMillisecond > 6000 && currentMillisecond < 8500) {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(gate0, ext, filter);
                        crowd.requestMoveTarget(1, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

                    /**
                     * agent 0 go to throw the trash
                     */
                    if (currentMillisecond > 3000 && currentMillisecond < 6000) {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(gate0, ext, filter);
                        crowd.requestMoveTarget(0, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

                    /**
                     * agent 1 go to throw the gate
                     */
                    if (currentMillisecond > 2000 && currentMillisecond < 6000) {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(gate1, ext, filter);
                        crowd.requestMoveTarget(1, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

                }

            //Now we actually run the simulation


//            int currentMillisecond = 0; //The current time
//            int millisecondsBetweenFrames = 40; //40ms between frames, or 25fps
//            int secondsOfSimulation = 20; //How long should the simulation run? Change this to whatever you want.
//            for (int i = 0; i < 25*secondsOfSimulation; i++) {
//
//                //Tell the crowd to update itself.
//                crowd.update(1 / 25f, null);
//
//                //Write the agents' positions in a file
//                writeAgentPosition(writer, currentMillisecond);
//                //Update the current simulation time
//                currentMillisecond+=millisecondsBetweenFrames;


                /**
                 * Here's where your code goes.
                 * Loop over the agents and, well, do whatever you want.
                 * Mainly you'll want to change agents' destinations as needed.
                 */


                /**
                 * 1. set final destination
                 * 2. agents have to walk through a "gate" which takes 3 to 5 secs per agent
                 * 3. thus set short-term destination to the gate
                 * 4. when agent arrives.(two condition)
                 *      1. no agent, go to gate directly
                 *      2. agents line up already, go to rear of the queue
                 */
                // line up

//                if (currentMillisecond >= 3000 && currentMillisecond < 5500) {
//                    float[] goTo = new float[]{0,0.31f,0};
//                    FindNearestPolyResult nearest = query.findNearestPoly(goTo, ext, filter);
//                    crowd.requestMoveTarget(2, nearest.getNearestRef(), nearest.getNearestPos());
//                }
            }
        } catch (IOException ignored) {

        }
    }
}
