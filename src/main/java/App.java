import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.Link;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Our crowd simulation class that extends CrowdSimApp.
 * CrowdSimApp is a wrapper around the Java version of Recast/Detour,
 * an open source crowd simulator.
 */
public class App extends CrowdSimApp{


    LinkedList<LinkedList<Integer>> QueueAll = new LinkedList<>();
    LinkedList<float[]> GateAll = new LinkedList<>();

    float[] gate = new float[]{-5, 0.31802097f, 8};
    float[] gate2 = new float[]{0, 0.31802097f, 8};
    float[] gate3 = new float[]{5, 0.31802097f, 8};
    public int agentCheckingTime = 4800;
    public int modParam = 5000;
    App() {

        // initialize queues in QueueAll
        for (int i = 0; i < 3; i++) {
            QueueAll.add(new LinkedList<>());
        }

        GateAll.add(gate);
        GateAll.add(gate2);
        GateAll.add(gate3);

        //Path is the path to the file where we will store our results
        //Currently, this is out.csv.
        Path path = bootFiles();

        //Open out.csv for writing and then run the simulation
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            //Boot simulation tells Recast to load the scene
            bootMesh();

            // store gate coordinate in array

            //Now we actually run the simulation

            int currentMillisecond = 0; //The current time
            int millisecondsBetweenFrames = 40; //40ms between frames, or 25fps
            int secondsOfSimulation = 20; //How long should the simulation run? Change this to whatever you want.

            // Randomly hold 4 agents at the beginning to simulate agent's coming sequence.
            int[] agentBeHoldList = generateHoldAgentList(10);

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

                    randomHoldAgent(agentBeHoldList, currentMillisecond);


                    float[] agentCur = getAgentCurrntPosition(j);

                    if (agent.getChecked()) {                                     // Checking process control
                        float[] finalDes = new float[]{8, 0.31802097f, 8};        // Checking process control
                        agentGoToGate(j, agent.getStart());                       // Checking process control
                    } else {
                        int randModParam = generateRandomNum(1500, 5500);
                        int index = pickGate(GateAll, j);
                        agentMove (j, GateAll.get(index), agentCur, agent, QueueAll.get(index), currentMillisecond % randModParam);
                    }
                }
            }
        } catch (IOException ignored) {

        }
        writeJSFile();
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Agent with specific id go to gate (destination).
     * @param agentId agentId agent go to gate
     * @param gate Destination where te specific agent will go.
     */
    private void agentGoToGate(int agentId, float[] gate) {
        FindNearestPolyResult nearest = query.findNearestPoly(gate, ext, filter);
        crowd.requestMoveTarget(agentId, nearest.getNearestRef(), nearest.getNearestPos());
    }

    /**
     * Get distance between agent's current position to the temporary destination.
     * @param aPos agent's current position.
     * @param des the temporary destination.
     * @return Distance of distance.
     */
    private static float getDis(float[] aPos, float[] des) {
        float dx = Math.abs(aPos[0] - des[0]);
        float dz = Math.abs(aPos[2] - des[2]);

        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Get position of rear of queue.
     * @param queueIdInput Queue list.
     * @return position of rear.
     */
    private float[] getRearBackPos(LinkedList<Integer> queueIdInput, float[] gateInput) {
        float[] des;
        if (queueIdInput.size() > 0) {
            int id = queueIdInput.getLast();
            des = getAgentBackPosition(id);
        } else {
            des = gateInput;
        }
        return des;
    }

    /**
     * Agent moves based on its checking/waiting status.
     * @param id
     * @param gateInput
     * @param agentCur
     * @param agent
     * @param queueIdInput
     * @param checkingTime
     */
    public void agentMove(int id, float[] gateInput, float[] agentCur, Agent agent, LinkedList<Integer> queueIdInput, int checkingTime) {
        ////////////       go to gate       ////////////
        if (queueIdInput.size() == 0 && !agent.getChecked()) { // no agent in line

            // no agent is checking, no queue
            agentGoToGate(id, gateInput);

            float dis = getDis(agentCur, gateInput);
            if (dis < 0.7f) {
                queueIdInput.add(agent.getId());
                agent.setWaiting();
            }
        }
        else {
            if (!agent.isWaiting() && !agent.getChecked())  { // not in line yet
                // Go to rear! Waiting line is too long?! Change line.

                ////////////////////////////////////////////////////////////////////////////////////
                // Go to the shorter line(length of line is larger than the other by 1 to 2)
                // Only work when there has two lines.
                LinkedList<Integer> otherQueue = compareQueueLine(QueueAll.get(0), QueueAll.get(1));
                float[] rearPos;
                if (otherQueue != null) {
                    // current rear position of queue
                    rearPos = getRearBackPos(otherQueue, gateInput);
                } else {
                    rearPos = getRearBackPos(queueIdInput, gateInput);
                }
                ////////////////////////////////////////////////////////////////////////////////////

                agentGoToGate(id, rearPos);

                float newDis = getDis(agentCur, rearPos);
                if (newDis < 0.7f) {
                    queueIdInput.add(agent.getId());
                    agent.setWaiting();
                }
            }
            else if (agent.isWaiting() && !agent.getChecked()) { // in line but not checked
                int idIndexInQueue = queueIdInput.indexOf(id);
                if (idIndexInQueue == 0) {
                    // get the head of queue
                    float newDis = getDis(agentCur, gateInput);

                    if (checkingTime > agentCheckingTime && newDis < 0.7f) {
                        agents.get(id).setChecked();                 // Checking process control
                        agents.get(id).setWaitingFalse();            // Checking process control
                        queueIdInput.removeFirst();                  // Checking process control
                    }
                }
                updateAgentInQueue(queueIdInput, id, checkingTime, gateInput); // Checking process control
                // here check the length of queues, make sure the difference among all queues is less than 2

//                if (QueueAll.get(0).size() > 1 && QueueAll.get(1).size() > 1 && QueueAll.get(2).size() > 1)
//                    adjustQueue(QueueAll);

            }
            else if (!agent.isWaiting() && agent.getChecked()) { // finish checked
                // finish check and then go to the end
            }
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

    //////////////////////////////////////////////////////////////////////////////////////////

    private void adjustQueue(LinkedList<LinkedList<Integer>> queueAllInput) {
        int longLine = queueAllInput.get(0).size(), shortLine = queueAllInput.get(0).size();
        int longIndex = 0, shortIndex = 0;
        for (int i = 1; i < queueAllInput.size(); i++) {
            if (longLine <= queueAllInput.get(i).size()) {
                longIndex = i;
            }
            if (shortLine >= queueAllInput.get(i).size()) {
                shortIndex = i;
            }
        }
        int diff = queueAllInput.get(longIndex).size() - queueAllInput.get(shortIndex).size();
        if (diff > 2) {
            LinkedList<Integer> longQueue = queueAllInput.get(longIndex);
            LinkedList<Integer> shortQueue = queueAllInput.get(shortIndex);
            int lastId_Long = longQueue.getLast();
            int lastId_Short = shortQueue.getLast();
            Agent lastAgent_Long = agents.get(lastId_Long);
            Agent lastAgent_Short = agents.get(lastId_Short);
            lastAgent_Long.setWaitingFalse();
//            agentGoToGate(longQueue.getLast(), );
            float[] newLocation = getAgentBackPosition(shortQueue.getLast());
            agentGoToGate(lastAgent_Long.getId(), newLocation);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    // Generate a agent list to be hold at the beginning.
    public int[] generateHoldAgentList(int numOfAgentHold) {
        int min = 0;
        int max = agents.size() - 1;
        int[] holdAgentList = new int[numOfAgentHold];

        for (int i = 0; i < numOfAgentHold; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
            holdAgentList[i] = randomNum;
        }
        return holdAgentList;
    }

    /**
     * This method is to randomly hold several agents at the begin
     * so agents won't start to move at the same time.
     * @param
     */
    private void randomHoldAgent(int[] agentHoldList, int currentMillisecond) {
        int[] holdingTimeList = {2500, 3000, 4000, 5000, 7000, 8500, 8800, 9000, 9100, 9500};
        for (int i = 0; i < agentHoldList.length; i++) {
            if (currentMillisecond < holdingTimeList[i]) {
                holdAgent(agentHoldList[i]);
            }
        }
    }

    private void holdAgent(int id) {
        FindNearestPolyResult nearest = query.findNearestPoly(agents.get(id).getStart(), ext, filter);
        crowd.requestMoveTarget(id, nearest.getNearestRef(), nearest.getNearestPos());
    }

    private LinkedList<Integer> compareQueueLine(LinkedList<Integer> qId1, LinkedList<Integer> qId2) {
        LinkedList<Integer> res;
        int absRes = Math.abs(qId1.size() - qId2.size());
        int diff = generateRandomNum(1, 2);
        if (absRes > diff) {
            // update line
            if (qId1.size() > qId2.size()) {
                res = qId2;
            } else {
                res = qId1;
            }
        }
        else {
            res = null;
        }
        return res;
    }

//    /**
//     * Decide which gate the agent will go.
//     * @param gate1
//     * @param gate2
//     * @param aID
//     * @return
//     */
//    private boolean whichGate(float[] gate1, float[] gate2, int aID) {
//        float[] aCurPos = getAgentCurrntPosition(aID);
//        float disToGate1 = getDis(aCurPos, gate1);
//        float disToGate2 = getDis(aCurPos, gate2);
//        boolean res;
//        if (disToGate1 > disToGate2){ // agent close to gate2
//            res = false;
//        } else { // agent close to gate1
//            res = true;
//        }
//        return res;
//    }

    /***
     * Return index of queue in QueueAll list and gate in GateAll list.
     */
    private int pickGate(LinkedList<float[]> gateAllInput, int aID) {
        float[] aCurPos = getAgentCurrntPosition(aID);
        float shortestDis = getDis(aCurPos, gate);
        int index = 0;
        for (int i = 0; i < gateAllInput.size(); i++) {
            if (getDis(aCurPos, gateAllInput.get(i)) < shortestDis) {
                shortestDis = getDis(aCurPos, gateAllInput.get(i));
                index = i;
            }
        }
        return index;
    }

    private int generateRandomNum(int min, int max) {
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        return randomNum;
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



