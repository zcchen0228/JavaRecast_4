import org.recast4j.detour.FindNearestPolyResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Random;

/**
 * Our crowd simulation class that extends CrowdSimApp.
 * CrowdSimApp is a wrapper around the Java version of Recast/Detour,
 * an open source crowd simulator.
 */
public class App extends CrowdSimApp{


    App() {


        Random rand = new Random();
        int ranNum = rand.nextInt(4);

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();

            boolean check = false;

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


                Agent agent0 = agents.get(0);
//                Agent agent1 = agents.get(1);
                Agent agent4 = agents.get(4);

                // format float[] {x, y, z};
                float[] gate = new float[]{8,0.31802097f,0};
                float[] trashCan = new float[]{0,0.31f,4}; // where the trash can is



                //At this point, we've loaded in.csv and store the starting positions/destinations
                //of each agent in a list called agents.
                //We'll loop over the agents and tell the crowd simulator
                //where each agent is and where each agent wants to go
                for(int j = 0; j < agents.size(); j++)
                {
                    float[] agentCur = getAgentCurrntPosition(j);

                    Agent agent = agents.get(j); //Grab each agent in the list
                    float[] start = agent.getStart();//Get the agent's starting point as a float array
                    crowd.addAgent(start, ap); //Assign that point to the agent

                    //Now find the nearest valid location to the agent's desired destination
                    //and assign that nearest point.
//                    if(check == false) {
                    if(!agents.get(j).getCheck()) {
                        FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
                        crowd.requestMoveTarget(j, nearest.getNearestRef(), nearest.getNearestPos());
                    } else {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(agent.getEnd(), ext, filter);
                        crowd.requestMoveTarget(j, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

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

                    /**
                     * stay in first 1500 millisec
                     * */

                    if(agentCur[0] > 6 && agentCur[2] > -0.1) {
                        agents.get(j).setCheck();
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
            }
        } catch (IOException ignored) {

        }
    }

    private void holdAgent(Agent agent, int agentNum) {
        FindNearestPolyResult nearest = query.findNearestPoly(agent.getStart(), ext, filter);
        crowd.requestMoveTarget(agentNum, nearest.getNearestRef(), nearest.getNearestPos());
    }
}