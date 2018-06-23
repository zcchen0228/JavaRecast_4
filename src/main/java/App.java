import org.recast4j.detour.FindNearestPolyResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Our crowd simulation class that extends CrowdSimApp.
 * CrowdSimApp is a wrapper around the Java version of Recast/Detour,
 * an open source crowd simulator.
 */
public class App extends CrowdSimApp{

    ArrayList<Agent> agentList = new ArrayList<>();
    ArrayList<Integer> agentNumList = new ArrayList<>();
    ArrayList<Float> agentPos = new ArrayList<>();
    ArrayList<Float> agentDis = new ArrayList<>();


    LinkedList<Agent> queue = new LinkedList<>();
    LinkedList<Integer> queueId = new LinkedList<>();

    App() {

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();


            //Now we actually run the simulation

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


                /**
                 * Here's where your code goes.
                 * Loop over the agents and, well, do whatever you want.
                 * Mainly you'll want to change agents' destinations as needed.
                 */

                for(int j = 0; j < agents.size(); j++) {
                    Agent agent = agents.get(j); //Grab each agent in the list
                    float[] start = agent.getStart();//Get the agent's starting point as a float array
                    crowd.addAgent(start, ap); //Assign that point to the agent

                    //////////////////////////////////////////////////////////
                    agent.setId(j);

                    float dis, newDis;

                    float[] agentCur = getAgentCurrntPosition(j);
                    if (queueId.size() == 0) {
                        float[] gate = new float[]{0, 0.31802097f, 8};
                        // no agent is checking, no queue
                        agentGoToGate(j, gate);
                        dis = getDis(agentCur, gate);
                        if (dis < 0.5f) {
                            queueId.add(agent.getId());
                            agent.setWaiting();
                        }
                    }
                    else {
                        if (agent.isWaiting()) {
                            lineAgentInQueue();
                        }
                        else {
                            // update destination, not gate!!! go to rear!!!!!
                            float[] newDes = getRearBackPos();

                            agentGoToGate(j, newDes);
                            newDis = getDis(agentCur, newDes);
                            if (newDis < 0.5f) {
                                queueId.add(agent.getId());
                                agent.setWaiting();
                            }
                        }

                    }
                    lineAgentInQueue();
                }

            }

        } catch (IOException ignored) {

        }
        writeJSFile();
    }


    public void agentGoToGate(int agentId, float[] gate) {
        FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
        crowd.requestMoveTarget(agentId, nearest.getNearestRef(), nearest.getNearestPos());
    }

    public void holdAgent(int agentId) {
        float[] curPos = getAgentCurrntPosition(agentId);
        FindNearestPolyResult nearest = query.findNearestPoly(curPos, ext, filter);
        crowd.requestMoveTarget(agentId, nearest.getNearestRef(), nearest.getNearestPos());
    }

    public static float getDis(float[] aPos, float[] tempDes) {
        float dx = Math.abs(aPos[0] - tempDes[0]);
//        float dy = Math.abs(aPos[1] - tempDes[1]);
        float dz = Math.abs(aPos[2] - tempDes[2]);

//        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    public float[] getRearBackPos() {
        float[] des;
        int id = queueId.getLast();
        des = getAgentBackPosition(id);
        return des;
    }


    private void lineAgentInQueue() {
        if (queueId.size() == 0) return;


        for (int i = 0; i < queueId.size() - 1; i++) {
            if (i == 0) {
                int id = queueId.get(i);
                holdAgent(id);
            }
            else {
                int id = queueId.get(i);
                int aNext = queueId.get(i + 1);
                float[] agentBack = getAgentBackPosition(id);
                FindNearestPolyResult nearest = query.findNearestPoly(agentBack, ext, filter);
                crowd.requestMoveTarget(aNext, nearest.getNearestRef(), nearest.getNearestPos());
            }
        }
    }

}
