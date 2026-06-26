package sh.smsh.ares.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

import java.util.List;

@Mixin(EntityArrow.class)
public abstract class MixinEntityArrow extends Entity {

    @Shadow private int xTile;
    @Shadow private int yTile;
    @Shadow private int zTile;
    @Shadow private Block inTile;
    @Shadow private int inData;
    @Shadow private boolean inGround;
    @Shadow public int arrowShake;
    @Shadow public Entity shootingEntity;
    @Shadow private int ticksInGround;
    @Shadow private int ticksInAir;
    @Shadow private double damage;
    @Shadow private int knockbackStrength;

    protected MixinEntityArrow(final World worldIn) {
        super(worldIn);
    }

    @Shadow public abstract boolean getIsCritical();

    @Shadow public abstract void setIsCritical(boolean critical);

    /**
     * @author Ares
     *
     * <p>
     *     @reason Remove the failed-hit arrow bounce branch while preserving vanilla arrow update behavior.
     * </p>
     * <p>
     *     Normally we'd just use some sort of injector and replace the fields with some other vars,
     *     however we really just don't want any part of that code branch to execute client-side.
     *     Since 1.8 arrows are rendered and behave the same, instead we can just mimic the entirety
     *     of the {@code onUpdate()} method and not include the animation code-block.
     * </p>
     * <p>
     *     This means we avoid relying on fragile field-write target matching injectors.
     * </p>
     */
    @Overwrite
    public void onUpdate() {
        super.onUpdate();
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (double)180.0F / (double)(float)Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (double)180.0F / (double)(float)Math.PI);
        }

        BlockPos blockPos = new BlockPos(this.xTile, this.yTile, this.zTile);
        IBlockState iBlockState = this.worldObj.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        if (block.getMaterial() != Material.air) {
            block.setBlockBoundsBasedOnState(this.worldObj, blockPos);
            AxisAlignedBB axisAlignedBB = block.getCollisionBoundingBox(this.worldObj, blockPos, iBlockState);
            if (axisAlignedBB != null && axisAlignedBB.isVecInside(new Vec3(this.posX, this.posY, this.posZ))) {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0) {
            --this.arrowShake;
        }

        if (this.inGround) {
            int i = block.getMetaFromState(iBlockState);
            if (block == this.inTile && i == this.inData) {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200) {
                    this.setDead();
                }
            } else {
                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        } else {
            ++this.ticksInAir;
            Vec3 vec3 = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 vec32 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingObjectPosition = this.worldObj.rayTraceBlocks(vec3, vec32, false, true, false);
            vec3 = new Vec3(this.posX, this.posY, this.posZ);
            vec32 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (movingObjectPosition != null) {
                vec32 = new Vec3(movingObjectPosition.hitVec.xCoord, movingObjectPosition.hitVec.yCoord, movingObjectPosition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand((double)1.0F, (double)1.0F, (double)1.0F));
            double d = (double)0.0F;

            for (int j = 0; j < list.size(); ++j) {
                Entity entity2 = list.get(j);
                if (entity2.canBeCollidedWith() && (entity2 != this.shootingEntity || this.ticksInAir >= 5)) {
                    float g = 0.3F;
                    AxisAlignedBB axisAlignedBB2 = entity2.getEntityBoundingBox().expand((double)g, (double)g, (double)g);
                    MovingObjectPosition movingObjectPosition2 = axisAlignedBB2.calculateIntercept(vec3, vec32);
                    if (movingObjectPosition2 != null) {
                        double e = vec3.squareDistanceTo(movingObjectPosition2.hitVec);
                        if (e < d || d == (double)0.0F) {
                            entity = entity2;
                            d = e;
                        }
                    }
                }
            }

            if (entity != null) {
                movingObjectPosition = new MovingObjectPosition(entity);
            }

            if (movingObjectPosition != null && movingObjectPosition.entityHit != null && movingObjectPosition.entityHit instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer)movingObjectPosition.entityHit;
                if (entityPlayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityPlayer)) {
                    movingObjectPosition = null;
                }
            }

            if (movingObjectPosition != null) {
                if (movingObjectPosition.entityHit != null) {
                    float h = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int k = MathHelper.ceiling_double_int((double)h * this.damage);
                    if (this.getIsCritical()) {
                        k += this.rand.nextInt(k / 2 + 2);
                    }

                    DamageSource damageSource;
                    if (this.shootingEntity == null) {
                        damageSource = DamageSource.causeArrowDamage((EntityArrow)(Object)this, (EntityArrow)(Object)this);
                    } else {
                        damageSource = DamageSource.causeArrowDamage((EntityArrow)(Object)this, this.shootingEntity);
                    }

                    if (this.isBurning() && !(movingObjectPosition.entityHit instanceof EntityEnderman)) {
                        movingObjectPosition.entityHit.setFire(5);
                    }

                    if (movingObjectPosition.entityHit.attackEntityFrom(damageSource, (float)k)) {
                        if (movingObjectPosition.entityHit instanceof EntityLivingBase) {
                            EntityLivingBase entityLivingBase = (EntityLivingBase)movingObjectPosition.entityHit;
                            if (!this.worldObj.isRemote) {
                                entityLivingBase.setArrowCountInEntity(entityLivingBase.getArrowCountInEntity() + 1);
                            }

                            if (this.knockbackStrength > 0) {
                                float l = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                                if (l > 0.0F) {
                                    movingObjectPosition.entityHit.addVelocity(this.motionX * (double)this.knockbackStrength * (double)0.6F / (double)l, 0.1, this.motionZ * (double)this.knockbackStrength * (double)0.6F / (double)l);
                                }
                            }

                            if (this.shootingEntity instanceof EntityLivingBase) {
                                EnchantmentHelper.applyThornEnchantments(entityLivingBase, this.shootingEntity);
                                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, entityLivingBase);
                            }

                            if (this.shootingEntity != null && movingObjectPosition.entityHit != this.shootingEntity && movingObjectPosition.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                                ((EntityPlayerMP)this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                        if (!(movingObjectPosition.entityHit instanceof EntityEnderman)) {
                            this.setDead();
                        }

                    } else if (!ServerGate.isAllowed() || !ModSettings.shouldDisableArrowBouncingAnimation()) {
                        this.motionX *= (double)-0.1F;
                        this.motionY *= (double)-0.1F;
                        this.motionZ *= (double)-0.1F;
                        this.rotationYaw += 180.0F;
                        this.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                    }

                } else {
                    BlockPos blockPos2 = movingObjectPosition.getBlockPos();
                    this.xTile = blockPos2.getX();
                    this.yTile = blockPos2.getY();
                    this.zTile = blockPos2.getZ();
                    IBlockState iBlockState2 = this.worldObj.getBlockState(blockPos2);
                    this.inTile = iBlockState2.getBlock();
                    this.inData = this.inTile.getMetaFromState(iBlockState2);
                    this.motionX = (double)((float)(movingObjectPosition.hitVec.xCoord - this.posX));
                    this.motionY = (double)((float)(movingObjectPosition.hitVec.yCoord - this.posY));
                    this.motionZ = (double)((float)(movingObjectPosition.hitVec.zCoord - this.posZ));
                    float g = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.posX -= this.motionX / (double)g * (double)0.05F;
                    this.posY -= this.motionY / (double)g * (double)0.05F;
                    this.posZ -= this.motionZ / (double)g * (double)0.05F;
                    this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.setIsCritical(false);
                    if (this.inTile.getMaterial() != Material.air) {
                        this.inTile.onEntityCollidedWithBlock(this.worldObj, blockPos2, iBlockState2, this);
                    }
                }
            }

            if (this.getIsCritical()) {
                for (int j = 0; j < 4; ++j) {
                    this.worldObj.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * (double)j / (double)4.0F, this.posY + this.motionY * (double)j / (double)4.0F, this.posZ + this.motionZ * (double)j / (double)4.0F, -this.motionX, -this.motionY + 0.2, -this.motionZ, new int[0]);
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float h = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (double)180.0F / (double)(float)Math.PI);

            for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)h) * (double)180.0F / (double)(float)Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float m = 0.99F;
            float g = 0.05F;
            if (this.isInWater()) {
                for (int n = 0; n < 4; ++n) {
                    float l = 0.25F;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)l, this.posY - this.motionY * (double)l, this.posZ - this.motionZ * (double)l, this.motionX, this.motionY, this.motionZ, new int[0]);
                }

                m = 0.6F;
            }

            if (this.isWet()) {
                this.extinguish();
            }

            this.motionX *= (double)m;
            this.motionY *= (double)m;
            this.motionZ *= (double)m;
            this.motionY -= (double)g;
            this.setPosition(this.posX, this.posY, this.posZ);
            this.doBlockCollisions();
        }
    }
}
