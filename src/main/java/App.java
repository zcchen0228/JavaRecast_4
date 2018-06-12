import org.recast4j.detour.FindNearestPolyResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.Random;

/**
 * Our crowd simulation class that extends CrowdSimApp.
 * CrowdSimApp is a wrapper around the Java version of Recast/Detour,
 * an open source crowd simulator.
 */
public class App extends CrowdSimApp{

    float[] gate = new float[]{8,0.31802097f,2};
    ArrayList<Agent> agentList = new ArrayList<Agent>();
    ArrayList<Integer> agentNumList = new ArrayList<Integer>();
    ArrayList<float[]> agentDis = new ArrayList<float[]>();

    App() {


        Random rand = new Random();
        int ranNum = rand.nextInt(4);

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        for (int i = 0; i < agents.size(); i++) {
            addAgent(agents.get(i), i, agents.get(i).getStart(), gate); // add to the ArrayList
        }

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();

            boolean check = false;

            int currentMillisecond = 0; //The current time
            int millisecondsBetweenFrames = 40; //40ms between frames, or 25fps
            int secondsOfSimulation = 20; //How long should the simulation run? Change this to whatever you want.
            for (int i = 0; i < 25 * secondsOfSimulation; i++) {

                //Tell the crowd to update itself.
                crowd.update(1 / 25f, null);

                //Write the agents' positions in a file
                writeAgentPosition(writer, currentMillisecond);
                //Update the current simulation time
                currentMillisecond += millisecondsBetweenFrames;

                // format float[] {x, y, z};

                float[] trashCan = new float[]{0,0.31f,4}; // where the trash can is

                //At this point, we've loaded in.csv and store the starting positions/destinations
                //of each agent in a list called agents.
                //We'll loop over the agents and tell the crowd simulator
                //where each agent is and where each agent wants to go
                for(int j = 0; j < agents.size(); j++) {

                    float[] agentCur = getAgentCurrntPosition(j);


                    Agent agent = agents.get(j); //Grab each agent in the list
                    float[] start = agent.getStart();//Get the agent's starting point as a float array
                    crowd.addAgent(start, ap); //Assign that point to the agent

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

                    //Now find the nearest valid location to the agent's desired destination
                    //and assign that nearest point.

                    if(!agents.get(j).getCheck()) {
                        FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
                        crowd.requestMoveTarget(j, nearest.getNearestRef(), nearest.getNearestPos());
                    } else {
                        FindNearestPolyResult nearestTemp = query.findNearestPoly(agent.getEnd(), ext, filter);
                        crowd.requestMoveTarget(j, nearestTemp.getNearestRef(), nearestTemp.getNearestPos());
                    }

                    // [0] ==> row       [2] ==> column
                    if(agentCur[0] > 6 && agentCur[2] > 2) {
//                        agents.get(j).setCheck();
                        agents.get(j).setRear();
                    }

//                    addAgent(agents.get(j), j, agentCur, gate); // add to the ArrayList

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                  calculate every agent's distance to destination and store in ArrayList in order
//                  start from the rear, set their own AgentInFront
//                  let agent stay at back of their AgentInFront while they are waiting
//                      --> destination of the first agent's is the gate
//                      --> the rest of them follow their own AgentInFront
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    lineAgent(j, agents.size());
                }
            }
        } catch (IOException ignored) {

        }
    }

    private void lineAgent(int j, int agentSize) {
        if (agentNumList.size() < agentSize) {
            return;
        }

        if (j == agentNumList.get(0)) {
            FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
            crowd.requestMoveTarget(agentNumList.get(0), nearest.getNearestRef(), nearest.getNearestPos());
        }

        for (int i = 1; i < agentNumList.size(); i++) {
            if (agentNumList.get(i) == j) {
                float[] agentBack = getAgentBackPosition(agentNumList.get(i - 1));
                FindNearestPolyResult nearest = query.findNearestPoly(agentBack, ext, filter);
                crowd.requestMoveTarget(agentNumList.get(i), nearest.getNearestRef(), nearest.getNearestPos());
            }
        }
    }

    /***
     * A helper method to help add all the agents.
     * @param agentNum
     * @param aPos
     * @param tempDes
     */
    public void addAgent(//ArrayList<float[]> agentDis,
                         Agent agent,
                         int agentNum,
                         float[] aPos,
                         float[] tempDes) {
        double newDis = getDis(aPos, tempDes);

        if (agentDis.size() == 0) {
            agentDis.add(aPos); // add agent's position
            agentNumList.add(agentNum); // add agent's number
            agentList.add(agent); // add agent
        }
        else {
            int pos = 0;
            for (int i = 0; i < agentDis.size(); i++) {
                if (getDis(agentDis.get(i), tempDes) > newDis) { // ascending order
                    break;
                }
                pos++;
            }
            if (pos == agentDis.size()) {
                agentDis.add(aPos); // add agent's position
                agentNumList.add(agentNum); // add agent's number
                agentList.add(agent); // add agent
            }
            else {
                agentDis.add(pos, aPos); // add agent's position
                agentNumList.add(pos, agentNum); // add agent's number
                agentList.add(pos, agent); // add agent
            }
        }
    }

    public static double getDis(float[] aPos, float[] tempDes) {
        double dis;
        float x = Math.abs(aPos[0] - tempDes[0]);
        float y = Math.abs(aPos[1] - tempDes[1]);

        return dis = Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) );
    }


    public boolean getRearAgent(boolean[] rearL) {
        boolean res = false;
        for (boolean t : rearL) if (t) return res;

        return res;
    }

}