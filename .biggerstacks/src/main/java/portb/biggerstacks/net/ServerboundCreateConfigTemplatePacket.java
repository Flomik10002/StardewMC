/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import portb.biggerstacks.Constants;
import portb.configlib.template.ConfigTemplate;
import portb.configlib.xml.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Supplier;

public class ServerboundCreateConfigTemplatePacket extends GenericTemplateOptionsPacket
{
    public ServerboundCreateConfigTemplatePacket(int normalStackLimit, int potionsStackLimit, int enchantedBooksStackLimit)
    {
        super(normalStackLimit, potionsStackLimit, enchantedBooksStackLimit);
    }
    
    public ServerboundCreateConfigTemplatePacket(FriendlyByteBuf buf)
    {
        super(buf);
    }
    
    static void handleCreateConfigTemplate(ServerboundCreateConfigTemplatePacket serverboundCreateConfigTemplatePacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        //If on a server, check that the player actually has permissions to do this
        if (FMLEnvironment.dist.isDedicatedServer())
        {
            ServerPlayer sender = contextSupplier.get().getSender();
            
            if (sender == null || !sender.hasPermissions(Constants.CHANGE_STACK_SIZE_COMMAND_PERMISSION_LEVEL))
                return;
        }
        
        ConfigTemplate template = ConfigTemplate.generateTemplate(serverboundCreateConfigTemplatePacket);
        
        if (ModList.get().isLoaded("ic2"))
        {
            //limit ic2 upgrades to 64 (if it is installed), or issues might occur with putting too many into machines
            template.getRules().add(new Rule(Arrays.asList(
                                            new Condition(Property.MOD_ID, Operator.EQUALS, "ic2"),
                                            new OrBlock(
                                                    Arrays.asList(
                                                            new Condition(Property.ID, Operator.STRING_STARTS_WITH, "ic2:upgrade"),
                                                            new Condition(Property.ID, Operator.STRING_ENDS_WITH, "pad_upgrade"),
                                                            new Condition(Property.ID, Operator.STRING_ENDS_WITH, "upgrade_kit")
                                                    )
                                            )
                                    ), 64)
            );
        }
        
        try
        {
            Files.writeString(Constants.RULESET_FILE,
                              template.toXML(),
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING
            );
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void encode(FriendlyByteBuf buf)
    {
        super.encode(buf);
    }
}