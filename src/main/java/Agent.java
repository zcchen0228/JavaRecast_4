public class Agent{
    public float startX;
    public float startY;
    public float startZ;
    public float destX;
    public float destY;
    public float destZ;
    public int startMSec;

    public boolean check = false;

    public Agent(String l) {
        String[] splits = l.split(",");

        startX = Float.parseFloat(splits[1]);
        startY = Float.parseFloat(splits[3]);
        startZ = Float.parseFloat(splits[2]);

        destX = Float.parseFloat(splits[4]);
        destY = Float.parseFloat(splits[6]);
        destZ = Float.parseFloat(splits[5]);

        startMSec = (int)Float.parseFloat(splits[1]);

        check = false;
    }

    public float[] getStart() {
        return new float[]{startX, startY, startZ};
    }

    public float[] getEnd() { return new float[]{destX, destY, destZ}; }

    public void setCheck() { check = true; }

    public boolean getCheck() { return check; }
}
