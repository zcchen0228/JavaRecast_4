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
    LinkedList<Integer> queueId2 = new LinkedList<>();

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

                    agent.setId(j);

                    float[] gate = new float[]{-3, 0.31802097f, 8};
                    float[] gate2 = new float[]{2, 0.31802097f, 8};

//                    float[] gate = pickGate(j);

                    float[] agentCur = getAgentCurrntPosition(j);

//                    agentMove (j, gate, agentCur, agent, queueId);

                    if (whichGate(gate, gate2, j)){ // true: go to gate1; false: go to gate2
                        ////////////       go to gate 1     ////////////
                        agentMove (j, gate, agentCur, agent, queueId, currentMillisecond % 2000);

                    } else { // true: go to gate1; false: go to gate2
                        ////////////       go to gate 2     ////////////
                        agentMove (j, gate2, agentCur, agent, queueId2, currentMillisecond % 2000);

                    }
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

    private void holdAgent(int agentId) {
        float[] curPos = getAgentCurrntPosition(agentId);
        FindNearestPolyResult nearest = query.findNearestPoly(curPos, ext, filter);
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

//    private void decideCheckingGate(float[] gate1, float[] gate2, int aID) {
//        float[] aCurPos = getAgentCurrntPosition(aID);
//        float disToGate1 = getDis(aCurPos, gate1);
//        float disToGate2 = getDis(aCurPos, gate2);
//
//        if (disToGate1 > disToGate2){ // agent close to gate2
//            agentGoToGate(aID, gate2);
//        } else { // agent close to gate1
//            agentGoToGate(aID, gate1);
//        }
//    }


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
//    public void agentMove(int id, float[] gate, float[] agentCur, Agent agent, LinkedList<Integer> queueId) {
        ////////////       go to gate       ////////////
        if (queueId.size() == 0) { // no agent in line

            // no agent is checking, no queue
            agentGoToGate(id, gate);

            float dis = getDis(agentCur, gate);
            if (dis < 0.7f) {
                queueId.add(agent.getId());
                agent.setWaiting();
            }
        } else { // has agent in line
            if (agent.isWaiting()) {
//                lineAgentInQueue(queueId, checkingTime);
                holdAgent(agent.getId());
            }
            else {
                // update destination, not gate! go to rear!
                float[] newDes = getRearBackPos(queueId);

                agentGoToGate(id, newDes);

                float newDis = getDis(agentCur, newDes);
                if (newDis < 0.7f) {
                    queueId.add(agent.getId());
                    agent.setWaiting();
                }
            }
        }
        ////////////       go to gate       ////////////
    }

    /**
     * agent in agentId List is set as waiting.
     *      Code: agent.isWaiting() is for specific agent, thus when loop through all agents, find
     *          1. check if it is the first one; check "checking" state;
     *          2. if it is checking ----> take 2 to 3 sec to move on
     *          3. update the line. 2nd replace the 1st agent. 
     *
     * */



//    private void lineAgentInQueue(LinkedList<Integer> queueId, int checkingTime) {
////    private void lineAgentInQueue(LinkedList<Integer> queue) {
//        if (queueId.size() == 0) return;
//
//
////        for (int i = 0; i < queueId.size() - 1; i++) {
////            if (i == 0) {
////                int id = queueId.get(i);
////
//                if (checkingTime < 1700) {
//                    agents.get(queueId.get(0)).setWaitingFalse();
//                }
//                Agent a = agents.get(queueId.get(0));
//                if (!a.isWaiting()) {
//                    FindNearestPolyResult nearest = query.findNearestPoly(agents.get(queueId.get(0)).getEnd(), ext, filter);
//                    crowd.requestMoveTarget(queueId.remove(0), nearest.getNearestRef(), nearest.getNearestPos());
//                }
////                if (checkingTime < 1700) {
////                    holdAgent(id);
////                } else {
//////                    int id = queueId.get(i);
//////                    int aNext = queueId.get(i + 1);
//////                    float[] agentBack = getAgentBackPosition(id);
////                    FindNearestPolyResult nearest = query.findNearestPoly(agents.get(queueId.get(0)).getEnd(), ext, filter);
////                    crowd.requestMoveTarget(queueId.remove(0), nearest.getNearestRef(), nearest.getNearestPos());
////                }
////            }
////            else {
////                int id = queueId.get(i);
////                int aNext = queueId.get(i + 1);
////                float[] agentBack = getAgentBackPosition(id);
////                FindNearestPolyResult nearest = query.findNearestPoly(agentBack, ext, filter);
////                crowd.requestMoveTarget(aNext, nearest.getNearestRef(), nearest.getNearestPos());
////            }
////        }
////        for (int i = 0; i < queueId.size() - 1; i++) {
////            if (i == 0) {
////                // agent move to it's own destination
////
////            }
////        }
//    }
}
