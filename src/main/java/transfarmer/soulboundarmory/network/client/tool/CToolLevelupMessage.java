package transfarmer.soulboundarmory.network.client.tool;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.SideOnly;
import transfarmer.soulboundarmory.client.i18n.Mappings;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public class CToolLevelupMessage implements IMessage {
    String stackName;
    int level;

    public CToolLevelupMessage() {}

    public CToolLevelupMessage(final ItemStack itemStack, final int level) {
        this.stackName = itemStack.getDisplayName();
        this.level = level;
    }

    @Override
    public void fromBytes(final ByteBuf buffer) {
        this.stackName = ByteBufUtils.readUTF8String(buffer);
        this.level = buffer.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.stackName);
        buffer.writeInt(this.level);
    }

    public static final class Handler implements IMessageHandler <CToolLevelupMessage, IMessage> {
        @SideOnly(CLIENT)
        @Override
        public IMessage onMessage(final CToolLevelupMessage message, final MessageContext context) {
            ((EntityPlayer) Minecraft.getMinecraft().player).sendMessage(
                    new TextComponentString(String.format(Mappings.MESSAGE_LEVEL_UP, message.stackName, message.level)));

            return null;
        }
    }
}