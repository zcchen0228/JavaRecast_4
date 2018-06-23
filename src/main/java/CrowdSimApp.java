import org.recast4j.detour.*;
import org.recast4j.detour.crowd.Crowd;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Created by bricks on 5/11/2018.
 */
public class CrowdSimApp {
    private final int updateFlags = CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS | CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS
            | CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO | CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE;
    protected NavMeshQuery query;
    protected Crowd crowd;
    protected List<Agent> agents = new ArrayList<>();
    protected float[] ext;
    protected QueryFilter filter;
    protected CrowdAgentParams ap;
    /**
     * Fields required to run the open source backend. There is no need to touch these.
     */
    private MeshData nmd;
    private NavMesh navmesh;

    //// zhicheng ///
    ArrayList<Agent> agentList = new ArrayList<>();
    ArrayList<Integer> agentNumList = new ArrayList<>();
    ArrayList<Float> agentPos = new ArrayList<>();
    ArrayList<float[]> agentDis = new ArrayList<>();
    //// zhicheng ////

    public static void main(String[] args) {

        //Call the constructor, which takes us out of a static context
        new App();
    }

    protected void bootMesh() throws FileNotFoundException {
        try {
            nmd = new RecastTestMeshBuilder().getMeshData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        nmd = new RecastTestMeshBuilder().getMeshData();
        navmesh = new NavMesh(nmd, 6, 0);
        query = new NavMeshQuery(navmesh);
        crowd = new Crowd(50, 0.6f, navmesh);
        ObstacleAvoidanceQuery.ObstacleAvoidanceParams params = new ObstacleAvoidanceQuery.ObstacleAvoidanceParams();
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 1;
        crowd.setObstacleAvoidanceParams(0, params);
        params = new ObstacleAvoidanceQuery.ObstacleAvoidanceParams();
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 2;
        crowd.setObstacleAvoidanceParams(1, params);
        params = new ObstacleAvoidanceQuery.ObstacleAvoidanceParams();
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 3;
        crowd.setObstacleAvoidanceParams(2, params);
        params = new ObstacleAvoidanceQuery.ObstacleAvoidanceParams();
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 3;
        params.adaptiveDepth = 3;
        crowd.setObstacleAvoidanceParams(3, params);

        ap = getAgentParams(updateFlags);
        ext = crowd.getQueryExtents();
        filter = crowd.getFilter(0);
    }

    private CrowdAgentParams getAgentParams(int updateFlags) {
        CrowdAgentParams ap = new CrowdAgentParams();
        ap.radius = 0.6f;
        ap.height = 2f;
        ap.maxAcceleration = 8.0f;
        ap.maxSpeed = 3.5f;
        ap.collisionQueryRange = ap.radius * 12f;
        ap.pathOptimizationRange = ap.radius * 30f;
        ap.updateFlags = updateFlags;
        ap.obstacleAvoidanceType = 0;
        ap.separationWeight = 2f;
        return ap;
    }

    protected void writeAgentPosition(BufferedWriter writer, int currentMillisecond) throws IOException {
        for(int j = 0; j < agents.size(); j++) {
            float x = crowd.getAgent(j).npos[0];
            float y = crowd.getAgent(j).npos[2];
            float z = crowd.getAgent(j).npos[1];

            writer.write("" + j +"," + currentMillisecond + "," + x + "," + y + "," + z + "\n");
        }
    }

    protected Path bootFiles() {
        try (Stream<String> stream = Files.lines(Paths.get("in.csv"))) {

            stream.forEach(l->agents.add(new Agent(l)));

        } catch (IOException e) {
            e.printStackTrace();
        }


        return Paths.get("out.csv");
    }

    protected void writeJSFile(){
        try {
            String content = new String(Files.readAllBytes(Paths.get("out.csv")));
            content = "data=`" + content + "`;";
            Files.write(Paths.get("data.js"), content.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    ///////////////////////////////////////zhi cheng/////////////////////////////////////////
    // a method to get the current position of certain agetn
    protected float[] getAgentCurrntPosition(int agentNum) {
        float[] res = {0,0,0};

        res[0] = crowd.getAgent(agentNum).npos[0];
        res[1] = crowd.getAgent(agentNum).npos[1];
        res[2] = crowd.getAgent(agentNum).npos[2];

        return res;
    }

    // a method to get the current behind position of certain agetn
    protected float[] getAgentBackPosition(int agentNum) {
        Random ran = new Random();
        float[] res = {0,0,0};

        res[0] = crowd.getAgent(agentNum).npos[0];
        res[1] = crowd.getAgent(agentNum).npos[1];
        res[2] = crowd.getAgent(agentNum).npos[2] - 1.5f;

        return res;
    }

    // sort agents by distance from current position to destination
//    protected ArrayList<Integer> sortNewAgentsList(List<Agent> Agents) {
//
//        // sort agent list
//        ArrayList<Integer> newArrList = new ArrayList<>();
//
//        for (int i = 0; i < Agents.size(); i++) {
//            addAgent(i, Agents.get(i), newArrList);
//        }
//
//        return newArrList;
//    }

//    private void addAgent(int agentNum, Agent a, ArrayList<Integer> arrL) {
//        if (arrL.size() == 0) arrL.add(agentNum);
//        else {
//            int pos = 0;
//            for (int i = 0; i < arrL.size(); i++) {
//                if (a.getDistance() < arrL.get(i).getDistance()) {
//                    break;
//                }
//                pos++;
//            }
//            if (pos == arrL.size()) arrL.add(a);
//            else arrL.add(pos, a);
//        }
//    }

//    public void addAgent(Agent agent, int agentNum, Float dis) {
//        agentList.add(agent);
//        agentNumList.add(agentNum);
//        agentPos.add(dis);
//        if(agentList.size() == 6) agentList.set(agentNum, agent);
//        if (agentNumList.size() == 6) agentNumList.set(agentNum, agentNum);
//        if (agentPos.size() == 6) agentPos.set(agentNum, dis);
//
//    }

//    public void sortAgent(ArrayList<Agent> agentList, ArrayList<Integer> agentNumList, ArrayList<Float> agentPos ) {
//        Agent keyAgent;
//        int keyANum;
//        float keyPos;
//
//        int j;
//        for (int i = 1; i < agentPos.size(); i++) {
//            keyAgent = agentList.get(i);
//            keyANum = agentNumList.get(i);
//            keyPos = agentPos.get(i);
//
//            j = i - 1;
//            while (j >= 0 && agentPos.get(j) > keyPos) {
//                agentList.set(j + 1, agentList.get(j));
//                agentNumList.set(j + 1, agentNumList.get(j));
//                agentPos.set(j + 1, agentPos.get(j));
//                j--;
//            }
//            agentList.set(j + 1, keyAgent);
//            agentNumList.set(j + 1, keyANum);
//            agentPos.set(j + 1, keyPos);
//        }
//    }
    ///////////////////////////////////////zhi cheng/////////////////////////////////////////
}
