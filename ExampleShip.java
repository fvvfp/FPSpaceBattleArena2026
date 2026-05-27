import java.awt.Color;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

public class ExampleShip extends BasicSpaceship {
   private int worldWidth;
   private int worldHeight;
   private Point center;
   
   boolean startup = true;
   private int ownerID;
   private double energyRechargeRate;
   private long mass;
   private int ID;
   private double maxEnergy;
   private double maxSpeed;

    public static void main(String[] args)
    {
        TextClient.run("10.56.98.121", new ExampleShip());
    }

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth_, int worldHeight_)
    {
      worldHeight = worldHeight_;
      worldWidth = worldWidth_;
      center = new Point(worldWidth / 2.0, worldHeight / 2.0);
      
      return new RegistrationData("FP Ship", new Color(205, 205, 255), 2);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment env)
    {
      ObjectStatus shipStatus = env.getShipStatus();
      
      if (startup) {
         ownerID = shipStatus.getOwnerId();
         energyRechargeRate = shipStatus.getRechargeRate();
         mass = shipStatus.getMass();
         ID = shipStatus.getId();
         maxEnergy = shipStatus.getMaxEnergy();
         maxSpeed = shipStatus.getMaxSpeed();
         startup = false;
      }
      
      double turnAngle = shipStatus.getPosition().getAngleTo(center);
      return new RotateCommand((int)turnAngle - shipStatus.getOrientation());
    }
}
