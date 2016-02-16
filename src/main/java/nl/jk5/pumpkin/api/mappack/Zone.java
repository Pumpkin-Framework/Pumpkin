package nl.jk5.pumpkin.api.mappack;

import com.flowpowered.math.vector.Vector3i;

public interface Zone {

    int getId();

    String getName();

    void setName(String name);

    int getPriority();

    void setPriority(int priority);

    int getX1();

    void setX1(int x1);

    int getY1();

    void setY1(int y1);

    int getZ1();

    void setZ1(int z1);

    int getX2();

    void setX2(int x2);

    int getY2();

    void setY2(int y2);

    int getZ2();

    void setZ2(int z2);

    Vector3i getStart();

    Vector3i getEnd();

    String getActionBlockPlace();

    void setActionBlockPlace(String actionBlockPlace);

    String getActionBlockBreak();

    void setActionBlockBreak(String actionBlockBreak);
}
