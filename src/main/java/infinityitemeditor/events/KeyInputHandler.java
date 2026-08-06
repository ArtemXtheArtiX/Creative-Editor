package infinityitemeditor.events;

import infinityitemeditor.InfinityItemEditor;
import infinityitemeditor.data.DataItem;
import infinityitemeditor.data.base.DataString;
import infinityitemeditor.data.base.DataUUID;
import infinityitemeditor.saving.DataItemCollection;
import infinityitemeditor.saving.SaveService;
import infinityitemeditor.screen.HeadCollectionScreen;
import infinityitemeditor.screen.MainScreen;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class KeyInputHandler {

    private static KeyBinding OPEN_EDITOR_KEY;
    private static KeyBinding OFF_HAND_SWING;
    public static KeyBinding HEAD_COLLECTION;
    public static KeyBinding BARRIER_TOGGLE;
    private static KeyBinding DEBUG_KEY;

    public static void init() {
        InfinityItemEditor.LOGGER.info("Initializing keybindings");
        OPEN_EDITOR_KEY = registerKeybind("editor", GLFW.GLFW_KEY_U);
        OFF_HAND_SWING = registerKeybind("offhandswing", InputMappings.UNKNOWN.getValue());
        HEAD_COLLECTION = registerKeybind("headcollection", GLFW.GLFW_KEY_V);
        BARRIER_TOGGLE = registerKeybind("barriertoggle", GLFW.GLFW_KEY_B);
        if (InfinityItemEditor.DEBUG)
            DEBUG_KEY = registerKeybind("debug", GLFW.GLFW_KEY_H);
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || event.getAction() != GLFW.GLFW_PRESS || (mc.screen != null && !(mc.screen instanceof ContainerScreen<?>)))
            return;

        if (event.getKey() == OPEN_EDITOR_KEY.getKey().getValue()) {
            assert mc.player != null;

            ItemStack itemToEdit = ItemStack.EMPTY;
            int slotIndex = -1;

            if (mc.screen instanceof ContainerScreen) {
                Slot hoveredSlot = getHoveredSlot(mc.screen);
                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    itemToEdit = hoveredSlot.getItem();
                    slotIndex = hoveredSlot.getSlotIndex();
                }
            }

            if (itemToEdit.isEmpty()) {
                itemToEdit = mc.player.getMainHandItem();
            }

            DataItem dataItem = new DataItem(itemToEdit);
            if (slotIndex >= 0) {
                dataItem.getSlot().set(slotIndex);
            }

            mc.setScreen(new MainScreen(mc.screen, dataItem));

        } else if (event.getKey() == OFF_HAND_SWING.getKey().getValue()) {
            assert mc.player != null;
            mc.player.swing(Hand.OFF_HAND);

        } else if (event.getKey() == HEAD_COLLECTION.getKey().getValue()) {
            HeadCollectionScreen headScreen = new HeadCollectionScreen(mc.screen);
            headScreen.ignoreKey = true;
            mc.setScreen(headScreen);

        } else if (event.getKey() == BARRIER_TOGGLE.getKey().getValue()) {
            InfinityItemEditor.BARRIER_VISIBLE = !InfinityItemEditor.BARRIER_VISIBLE;
            mc.levelRenderer.allChanged();

        } else if (InfinityItemEditor.DEBUG && event.getKey() == DEBUG_KEY.getKey().getValue()) {
            ItemStack stack = mc.player.getMainHandItem();
            if (stack.getItem() == Blocks.PURPLE_SHULKER_BOX.asItem()) {
                DataItem item = new DataItem(stack);
                File file = new File(SaveService.getInstance().getItemCollectionsDir(), new DataUUID().getData().toString() + ".nbt");
                DataString name = new DataString(stack.getHoverName().getString());
                DataItemCollection itemCollection = DataItemCollection.fromBlockEntityTag(file, item.getTag().getBlockEntityTag(), name);
                try {
                    itemCollection.save();
                    SaveService.getInstance().getItemCollections().add(itemCollection);
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("Added"), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Slot getHoveredSlot(Screen screen) {
        try {
            Field field = ContainerScreen.class.getDeclaredField("hoveredSlot");
            field.setAccessible(true);
            return (Slot) field.get(screen);
        } catch (NoSuchFieldException e1) {
            try {
                Field field = ContainerScreen.class.getDeclaredField("field_147006_u");
                field.setAccessible(true);
                return (Slot) field.get(screen);
            } catch (Exception e2) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static KeyBinding registerKeybind(String name, int keyCode) {
        KeyBinding key = new KeyBinding("key." + name, keyCode, InfinityItemEditor.NAME);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}