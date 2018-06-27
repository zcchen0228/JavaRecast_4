public class Agent{
    public float startX;
    public float startY;
    public float startZ;
    public float destX;
    public float destY;
    public float destZ;
    public int startMSec;

    public int id;
    public boolean queueExist;
    public boolean checked;
    public boolean checking;
    public boolean rear;
    public boolean waiting;
    public Agent agentInFront;


    public float dis;

    public Agent(String l) {
        String[] splits = l.split(",");

        startX = Float.parseFloat(splits[1]);
        startY = Float.parseFloat(splits[3]);
        startZ = Float.parseFloat(splits[2]);

        destX = Float.parseFloat(splits[4]);
        destY = Float.parseFloat(splits[6]);
        destZ = Float.parseFloat(splits[5]);

        startMSec = (int)Float.parseFloat(splits[1]);

        rear = false;
        queueExist = false;
        checked = false;
        checking = false;
        rear = false;
        waiting = false;
        dis = 0;
    }

    public float[] getStart() {return new float[]{startX, startY, startZ};}
    public float[] getEnd() {return new float[]{destX, destY, destZ};}

    public void setAgentInFront (Agent a) { agentInFront = a; }
    public Agent getAgentInFront (Agent a) { return agentInFront; }

    public void setChecked() { checked = true; }
    public boolean getChecked() { return checked; }

    public void setChecking() { checked = true; }
    public boolean getChecking() { return checked; }

    public float getDistance() { return dis; }
    public void setDis(float dis) { this.dis = dis; }

//    public boolean getQueueExist() {
//
//        return this.queueExist;
//    }

    public void setId(int i) { id = i; }
    public int getId() { return id; }

    public void setWaiting() { waiting = true; }
    public void setWaitingFalse() { waiting = false; }
    public boolean isWaiting() { return waiting; }

}