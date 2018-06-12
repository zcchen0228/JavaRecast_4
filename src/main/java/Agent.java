public class Agent{
    public float startX;
    public float startY;
    public float startZ;
    public float destX;
    public float destY;
    public float destZ;
    public int startMSec;

    public boolean check;
    public boolean checking;
    public boolean rear;
    public boolean waiting;
    public Agent agentInFront;

    public Agent(String l) {
        agentInFront = null;
        String[] splits = l.split(",");

        startX = Float.parseFloat(splits[1]);
        startY = Float.parseFloat(splits[3]);
        startZ = Float.parseFloat(splits[2]);

        destX = Float.parseFloat(splits[4]);
        destY = Float.parseFloat(splits[6]);
        destZ = Float.parseFloat(splits[5]);

        startMSec = (int)Float.parseFloat(splits[1]);

        check = false;
        checking = false;
        rear = false;
        waiting = false;

    }

    public void setAgentInFront (Agent a) { agentInFront = a; }
    public Agent getAgentInFront (Agent a) { return agentInFront; }

    public float[] getStart() {
        return new float[]{startX, startY, startZ};
    }
    public float[] getEnd() { return new float[]{destX, destY, destZ}; }

    public void setCheck() { check = true; }
    public boolean getCheck() { return check; }

    public void setRear() { rear = true; }
    public void setNotRear() { rear = false; }
    public boolean getRear() { return rear; }


    public void setChecking() { checking = true; }
    public boolean getChecking() { return checking; }

    public void setWaiting() { check = true; }
    public boolean getWainting() { return check; }
}
