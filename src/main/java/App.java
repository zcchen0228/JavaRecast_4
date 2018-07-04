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

    LinkedList<Integer> queueId = new LinkedList<>();
    LinkedList<Integer> queueId2 = new LinkedList<>();
    public int agentCheckingTime = 3800;

    App() {

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();

            // store gate coordinate in array
//            for ()

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
                currentMillisecond += millisecondsBetweenFrames;


                /**
                 * Here's where your code goes.
                 * Loop over the agents and, well, do whatever you want.
                 * Mainly you'll want to change agents' destinations as needed.
                 */

                for(int j = 0; j < agents.size(); j++) {
                    Agent agent = agents.get(j); //Grab each agent in the list
                    float[] start = agent.getStart();//Get the agent's starting point as a float array
                    crowd.addAgent(start, ap); //Assign that point to the agent

                    agent.setId(j);

                    float[] gate = new float[]{-5, 0.31802097f, 6};
                    float[] gate2 = new float[]{-2, 0.31802097f, 6};

                    float[] agentCur = getAgentCurrntPosition(j);

                    if (whichGate(gate, gate2, j)){ // true: go to gate1; false: go to gate2
                        ////////////       go to gate 1     ////////////
                        agentMove (j, gate, agentCur, agent, queueId, currentMillisecond % 4000);

                    } else { // true: go to gate1; false: go to gate2
                        ////////////       go to gate 2     ////////////
                        agentMove (j, gate2, agentCur, agent, queueId2, currentMillisecond % 4000);
                    }

//                    if (agent.getChecked()) {                                     // Checking process control
//                        float[] finalDes = new float[]{8, 0.31802097f, 8};        // Checking process control
//                        agentGoToGate(j, finalDes);                               // Checking process control
//                    }                                                             // Checking process control
                }
            }
        } catch (IOException ignored) {

        }
        writeJSFile();
    }


    private void agentGoToGate(int agentId, float[] gate) {
        FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
        crowd.requestMoveTarget(agentId, nearest.getNearestRef(), nearest.getNearestPos());
    }

    private static float getDis(float[] aPos, float[] tempDes) {
        float dx = Math.abs(aPos[0] - tempDes[0]);
        float dz = Math.abs(aPos[2] - tempDes[2]);

        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    private float[] getRearBackPos(LinkedList<Integer> queueId) {
        float[] des;
        int id = queueId.getLast();
        des = getAgentBackPosition(id);
        return des;
    }

    private boolean whichGate(float[] gate1, float[] gate2, int aID) {
        float[] aCurPos = getAgentCurrntPosition(aID);
        float disToGate1 = getDis(aCurPos, gate1);
        float disToGate2 = getDis(aCurPos, gate2);
        boolean res;
        if (disToGate1 > disToGate2){ // agent close to gate2
            res = false;
        } else { // agent close to gate1
            res = true;
        }
        return res;
    }

    public void agentMove(int id, float[] gate, float[] agentCur, Agent agent, LinkedList<Integer> queueId, int checkingTime) {
        ////////////       go to gate       ////////////
        if (queueId.size() == 0 && !agent.getChecked()) { // no agent in line

            // no agent is checking, no queue
            agentGoToGate(id, gate);

            float dis = getDis(agentCur, gate);
            if (dis < 0.7f) {
                queueId.add(agent.getId());
                agent.setWaiting();
            }
        }
        else {
            if (!agent.isWaiting() && !agent.getChecked())  { // not in line yet
                // update destination, not gate! go to rear!
                // getRearBackPos() return current rear position of queue
                float[] newDes = getRearBackPos(queueId);

                agentGoToGate(id, newDes);

                float newDis = getDis(agentCur, newDes);
                if (newDis < 0.7f) {
                    queueId.add(agent.getId());
                    agent.setWaiting();
                }
            }
            else if (agent.isWaiting() && !agent.getChecked()) { // in line but not checked
                int idIndexInQueue = queueId.indexOf(id);
                if (idIndexInQueue == 0) {
                    // get the head of queue
                    float newDis = getDis(agentCur, gate);
                    if (checkingTime > agentCheckingTime && newDis < 0.7f) {
//                        agents.get(id).setChecked();                 // Checking process control
//                        agents.get(id).setWaitingFalse();            // Checking process control
//                        queueId.removeFirst();                       // Checking process control
                    }
                }
//                updateAgentInQueue(queueId, id, checkingTime, gate); // Checking process control
            }
            else if (!agent.isWaiting() && agent.getChecked()) { // finish checked
                // finish check and then go to the end
            }
        }

        if(agent.getChecked()) {
            FindNearestPolyResult nearest = query.findNearestPoly(agent.getStart(), ext, filter);
            crowd.requestMoveTarget(id, nearest.getNearestRef(), nearest.getNearestPos());
        }
        ////////////       go to gate       ////////////
    }

    private void updateAgentInQueue(LinkedList<Integer> queueId, int id, int checkingTime, float[] gate) {
        if (queueId.size() == 0) return;

        if (id == queueId.get(0)) {
            int firstId = queueId.getFirst();
            float disToGate = getDis(getAgentCurrntPosition(firstId), gate);
            if (disToGate < 0.7f && checkingTime > agentCheckingTime) {
                agents.get(id).setChecked();
            } else {
                agentGoToGate(id, gate);
            }
        }
        else {
            for (int i = 1; i < queueId.size(); i++) {
                float[] prevAgentPos = getAgentBackPosition(queueId.get(i - 1));
                agentGoToGate(queueId.get(i), prevAgentPos);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private float[] pickGate(int aID) {
        ArrayList<float[]> gates = new ArrayList<>();
        float[] gate1 = new float[]{-3, 0.31802097f, 8};
        float[] gate2 = new float[]{0, 0.31802097f, 8};
        float[] gate3 = new float[]{3, 0.31802097f, 8};
        gates.add(gate1);
        gates.add(gate2);
        gates.add(gate3);

        float[] aCurPos = getAgentCurrntPosition(aID);

        float dis1 = getDis(aCurPos, gate1); // distance to gate1, in index 0
        int gateNum = 0;

        for (int i = 1; i < gates.size(); i++) {
            float dis2 = getDis(aCurPos,gates.get(i));
            if (dis1 > dis2) {
                gateNum = i;
            }
        }
        return gates.get(gateNum);
    }

    private void holdAgent(int agentId) {
        float[] curPos = getAgentCurrntPosition(agentId);
        FindNearestPolyResult nearest = query.findNearestPoly(curPos, ext, filter);
        crowd.requestMoveTarget(agentId, nearest.getNearestRef(), nearest.getNearestPos());
    }

    ////////////////////////////////////////////////////////////////////////////////////////
}

/**
 *
 * agent：
 *      if ( 有 gate 为空)
 *          去 gate
 *      else if ( 没有 gate 为空)
 *          看哪条 line 最短，排到最短的 line
 *
 *
 * */



