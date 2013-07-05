package maxpowa.tukmc;

import java.awt.Color;
import java.util.List;

import maxpowa.codebase.client.ClientUtils;
import maxpowa.codebase.common.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

public class MobHealthBars {
	
	@ForgeSubscribe
	public void onRenderWorldLast(RenderWorldLastEvent event) {		
		Minecraft mc = CommonUtils.getMc();

		if (!Minecraft.isGuiEnabled() /*|| TODO: Add Config*/) return;

		EntityLivingBase cameraEntity = mc.renderViewEntity;
		Vec3 renderingVector = cameraEntity.getPosition(event.partialTicks);
		Frustrum frustrum = new Frustrum();
		

		double viewX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * event.partialTicks;
		double viewY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * event.partialTicks;
		double viewZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * event.partialTicks;
		frustrum.setPosition(viewX, viewY, viewZ);

		List<Entity> loadedEntities = mc.theWorld.getLoadedEntityList();
		for (Entity entity : loadedEntities)
			if (entity != null && entity instanceof EntityLivingBase && entity.isInRangeToRenderVec3D(renderingVector) && (entity.ignoreFrustumCheck || frustrum.isBoundingBoxInFrustum(entity.boundingBox)) && entity.isEntityAlive()) 
				renderHealthBar((EntityLivingBase) entity, event.partialTicks, cameraEntity);
	}

	public void renderHealthBar(EntityLivingBase entity, float partialTicks, Entity viewPoint) {
		Minecraft mc = CommonUtils.getMc();
		
		float distance = entity.getDistanceToEntity(viewPoint);
		if (distance > 20 || !entity.canEntityBeSeen(viewPoint) || entity == viewPoint || entity.riddenByEntity == viewPoint) 
			return;
				
		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

		float scale = 0.026666672F;
		float maxHealth = entity.func_110138_aP();
		float health = entity.func_110143_aJ();

		renderLabel(entity, String.format("%s/%s (%s", health, maxHealth, maxHealth == 0 ? 0 : (health * 100 / maxHealth)) + "%)", (float) (x - RenderManager.renderPosX), (float) (y - RenderManager.renderPosY + entity.height + 0.9), (float) (z - RenderManager.renderPosZ), 20);

		GL11.glPushMatrix();
		GL11.glTranslatef((float) (x - RenderManager.renderPosX), (float) (y - RenderManager.renderPosY + entity.height + 0.6), (float) (z - RenderManager.renderPosZ));
		GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scale, -scale, scale);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawDoubleOutlinedBox(-(int) maxHealth / 2, -1, 3, (int) maxHealth, 2, TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
        drawSolidRect(-(int) maxHealth, -2, 3, (int) health, 2, 0xFF << 16);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}
	
	public void drawDoubleOutlinedBox(final int x, final int y,
            final int z, final int width, final int height, final int color,
            final int outlineColor) {
        drawDoubleOutlinedBox(x, y, z, width, height, color, outlineColor, color);
    }

    public void drawDoubleOutlinedBox(final int x, final int y,
            final int z, final int width, final int height, final int color,
            final int outlineColor, final int outline2Color) {
        drawSolidRect(x * 2 - 2, y * 2 - 2, z, (x + width) * 2 + 2,
                (y + height) * 2 + 2, color);
        drawSolidRect(x * 2 - 1, y * 2 - 1, z, (x + width) * 2 + 1,
                (y + height) * 2 + 1, outlineColor);
        drawSolidRect(x * 2, y * 2, z, (x + width) * 2, (y + height) * 2,
                outline2Color);
    }

    public void drawSolidRect(final int vertex1, final int vertex2, final int zLevel,
            final int vertex3, final int vertex4, final int color) {
        GL11.glPushMatrix();
        final Color color1 = new Color(color);
        final Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorOpaque(color1.getRed(), color1.getGreen(),
                color1.getBlue());
        tess.addVertex(vertex1, vertex4, zLevel);
        tess.addVertex(vertex3, vertex4, zLevel);
        tess.addVertex(vertex3, vertex2, zLevel);
        tess.addVertex(vertex1, vertex2, zLevel);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

	public static void renderLabel(EntityLivingBase par1EntityLiving, String par2Str, double par3, double par5, double par7, int par9) {
		Minecraft mc = CommonUtils.getMc();
		RenderManager renderManager = RenderManager.instance;

		if (renderManager.livingPlayer == null || par1EntityLiving == null) return;

		double var10 = par1EntityLiving.getDistanceSqToEntity(renderManager.livingPlayer);

		if (var10 <= par9 * par9) {
			FontRenderer var12 = mc.fontRenderer;
			float var13 = 1.6F;
			float var14 = 0.016666668F * var13;
			GL11.glPushMatrix();
			GL11.glTranslatef((float) par3, (float) par5, (float) par7);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-var14, -var14, var14);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator var15 = Tessellator.instance;
			byte var16 = 0;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			var15.startDrawingQuads();
			int var17 = var12.getStringWidth(par2Str) / 2;
			var15.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
			var15.addVertex(-var17 - 1, -1 + var16, 0.0D);
			var15.addVertex(-var17 - 1, 8 + var16, 0.0D);
			var15.addVertex(var17 + 1, 8 + var16, 0.0D);
			var15.addVertex(var17 + 1, -1 + var16, 0.0D);
			var15.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16, 553648127);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16, -1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}
	
}