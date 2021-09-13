package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class JobPuppet extends JobInterface implements IJobPuppet {
     public JobPuppet.PartConfig head = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig larm = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig rarm = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig body = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig lleg = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig rleg = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig head2 = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig larm2 = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig rarm2 = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig body2 = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig lleg2 = new JobPuppet.PartConfig();
     public JobPuppet.PartConfig rleg2 = new JobPuppet.PartConfig();
     public boolean whileStanding = true;
     public boolean whileAttacking = false;
     public boolean whileMoving = false;
     public boolean animate = false;
     public int animationSpeed = 4;
     private int prevTicks = 0;
     private int startTick = 0;
     private float val = 0.0F;
     private float valNext = 0.0F;

     public JobPuppet(EntityNPCInterface npc) {
          super(npc);
     }

     public IJobPuppet.IJobPuppetPart getPart(int part) {
          if (part == 0) {
               return this.head;
          } else if (part == 1) {
               return this.larm;
          } else if (part == 2) {
               return this.rarm;
          } else if (part == 3) {
               return this.body;
          } else if (part == 4) {
               return this.lleg;
          } else if (part == 5) {
               return this.rleg;
          } else if (part == 6) {
               return this.head2;
          } else if (part == 7) {
               return this.larm2;
          } else if (part == 8) {
               return this.rarm2;
          } else if (part == 9) {
               return this.body2;
          } else if (part == 10) {
               return this.lleg2;
          } else if (part == 11) {
               return this.rleg2;
          } else {
               throw new CustomNPCsException("Unknown part " + part, new Object[0]);
          }
     }

     public NBTTagCompound writeToNBT(NBTTagCompound compound) {
          compound.func_74782_a("PuppetHead", this.head.writeNBT());
          compound.func_74782_a("PuppetLArm", this.larm.writeNBT());
          compound.func_74782_a("PuppetRArm", this.rarm.writeNBT());
          compound.func_74782_a("PuppetBody", this.body.writeNBT());
          compound.func_74782_a("PuppetLLeg", this.lleg.writeNBT());
          compound.func_74782_a("PuppetRLeg", this.rleg.writeNBT());
          compound.func_74782_a("PuppetHead2", this.head2.writeNBT());
          compound.func_74782_a("PuppetLArm2", this.larm2.writeNBT());
          compound.func_74782_a("PuppetRArm2", this.rarm2.writeNBT());
          compound.func_74782_a("PuppetBody2", this.body2.writeNBT());
          compound.func_74782_a("PuppetLLeg2", this.lleg2.writeNBT());
          compound.func_74782_a("PuppetRLeg2", this.rleg2.writeNBT());
          compound.func_74757_a("PuppetStanding", this.whileStanding);
          compound.func_74757_a("PuppetAttacking", this.whileAttacking);
          compound.func_74757_a("PuppetMoving", this.whileMoving);
          compound.func_74757_a("PuppetAnimate", this.animate);
          compound.func_74768_a("PuppetAnimationSpeed", this.animationSpeed);
          return compound;
     }

     public void readFromNBT(NBTTagCompound compound) {
          this.head.readNBT(compound.func_74775_l("PuppetHead"));
          this.larm.readNBT(compound.func_74775_l("PuppetLArm"));
          this.rarm.readNBT(compound.func_74775_l("PuppetRArm"));
          this.body.readNBT(compound.func_74775_l("PuppetBody"));
          this.lleg.readNBT(compound.func_74775_l("PuppetLLeg"));
          this.rleg.readNBT(compound.func_74775_l("PuppetRLeg"));
          this.head2.readNBT(compound.func_74775_l("PuppetHead2"));
          this.larm2.readNBT(compound.func_74775_l("PuppetLArm2"));
          this.rarm2.readNBT(compound.func_74775_l("PuppetRArm2"));
          this.body2.readNBT(compound.func_74775_l("PuppetBody2"));
          this.lleg2.readNBT(compound.func_74775_l("PuppetLLeg2"));
          this.rleg2.readNBT(compound.func_74775_l("PuppetRLeg2"));
          this.whileStanding = compound.func_74767_n("PuppetStanding");
          this.whileAttacking = compound.func_74767_n("PuppetAttacking");
          this.whileMoving = compound.func_74767_n("PuppetMoving");
          this.setIsAnimated(compound.func_74767_n("PuppetAnimate"));
          this.setAnimationSpeed(compound.func_74762_e("PuppetAnimationSpeed"));
     }

     public boolean aiShouldExecute() {
          return false;
     }

     private float calcRotation(float r, float r2, float partialTicks) {
          if (!this.animate) {
               return r;
          } else {
               float speed;
               if (this.prevTicks != this.npc.field_70173_aa) {
                    speed = 0.0F;
                    if (this.animationSpeed == 0) {
                         speed = 40.0F;
                    } else if (this.animationSpeed == 1) {
                         speed = 24.0F;
                    } else if (this.animationSpeed == 2) {
                         speed = 13.0F;
                    } else if (this.animationSpeed == 3) {
                         speed = 10.0F;
                    } else if (this.animationSpeed == 4) {
                         speed = 7.0F;
                    } else if (this.animationSpeed == 5) {
                         speed = 4.0F;
                    } else if (this.animationSpeed == 6) {
                         speed = 3.0F;
                    } else if (this.animationSpeed == 7) {
                         speed = 2.0F;
                    }

                    int ticks = this.npc.field_70173_aa - this.startTick;
                    this.val = 1.0F - (MathHelper.func_76134_b((float)ticks / speed * 3.1415927F / 2.0F) + 1.0F) / 2.0F;
                    this.valNext = 1.0F - (MathHelper.func_76134_b((float)(ticks + 1) / speed * 3.1415927F / 2.0F) + 1.0F) / 2.0F;
                    this.prevTicks = this.npc.field_70173_aa;
               }

               speed = this.val + (this.valNext - this.val) * partialTicks;
               return r + (r2 - r) * speed;
          }
     }

     public float getRotationX(JobPuppet.PartConfig part1, JobPuppet.PartConfig part2, float partialTicks) {
          return this.calcRotation(part1.rotationX, part2.rotationX, partialTicks);
     }

     public float getRotationY(JobPuppet.PartConfig part1, JobPuppet.PartConfig part2, float partialTicks) {
          return this.calcRotation(part1.rotationY, part2.rotationY, partialTicks);
     }

     public float getRotationZ(JobPuppet.PartConfig part1, JobPuppet.PartConfig part2, float partialTicks) {
          return this.calcRotation(part1.rotationZ, part2.rotationZ, partialTicks);
     }

     public void reset() {
          this.val = 0.0F;
          this.valNext = 0.0F;
          this.prevTicks = 0;
          this.startTick = this.npc.field_70173_aa;
     }

     public void delete() {
     }

     public boolean isActive() {
          if (!this.npc.func_70089_S()) {
               return false;
          } else {
               return this.whileAttacking && this.npc.isAttacking() || this.whileMoving && this.npc.isWalking() || this.whileStanding && !this.npc.isWalking();
          }
     }

     public boolean getIsAnimated() {
          return this.animate;
     }

     public void setIsAnimated(boolean bo) {
          this.animate = bo;
          if (!bo) {
               this.val = 0.0F;
               this.valNext = 0.0F;
               this.prevTicks = 0;
          } else {
               this.startTick = this.npc.field_70173_aa;
          }

          this.npc.updateClient = true;
     }

     public int getAnimationSpeed() {
          return this.animationSpeed;
     }

     public void setAnimationSpeed(int speed) {
          this.animationSpeed = ValueUtil.CorrectInt(speed, 0, 7);
          this.npc.updateClient = true;
     }

     public class PartConfig implements IJobPuppet.IJobPuppetPart {
          public float rotationX = 0.0F;
          public float rotationY = 0.0F;
          public float rotationZ = 0.0F;
          public boolean disabled = false;

          public NBTTagCompound writeNBT() {
               NBTTagCompound compound = new NBTTagCompound();
               compound.func_74776_a("RotationX", this.rotationX);
               compound.func_74776_a("RotationY", this.rotationY);
               compound.func_74776_a("RotationZ", this.rotationZ);
               compound.func_74757_a("Disabled", this.disabled);
               return compound;
          }

          public void readNBT(NBTTagCompound compound) {
               this.rotationX = ValueUtil.correctFloat(compound.func_74760_g("RotationX"), -1.0F, 1.0F);
               this.rotationY = ValueUtil.correctFloat(compound.func_74760_g("RotationY"), -1.0F, 1.0F);
               this.rotationZ = ValueUtil.correctFloat(compound.func_74760_g("RotationZ"), -1.0F, 1.0F);
               this.disabled = compound.func_74767_n("Disabled");
          }

          public int getRotationX() {
               return (int)((this.rotationX + 1.0F) * 180.0F);
          }

          public int getRotationY() {
               return (int)((this.rotationY + 1.0F) * 180.0F);
          }

          public int getRotationZ() {
               return (int)((this.rotationZ + 1.0F) * 180.0F);
          }

          public void setRotation(int x, int y, int z) {
               this.disabled = false;
               this.rotationX = ValueUtil.correctFloat((float)x / 180.0F - 1.0F, -1.0F, 1.0F);
               this.rotationY = ValueUtil.correctFloat((float)y / 180.0F - 1.0F, -1.0F, 1.0F);
               this.rotationZ = ValueUtil.correctFloat((float)z / 180.0F - 1.0F, -1.0F, 1.0F);
               JobPuppet.this.npc.updateClient = true;
          }
     }
}
