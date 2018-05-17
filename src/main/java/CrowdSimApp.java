import org.recast4j.detour.*;
import org.recast4j.detour.crowd.Crowd;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

            writer.write("" + j +"," + currentMillisecond + "," + x + "," + y + "\n");
        }
    }

    protected Path bootFiles() {
        try (Stream<String> stream = Files.lines(Paths.get("in.csv"))) {

            stream.forEach(l -> agents.add(new Agent(l)));


        } catch (IOException e) {
            e.printStackTrace();
        }

        return Paths.get("out.csv");
    }
}
