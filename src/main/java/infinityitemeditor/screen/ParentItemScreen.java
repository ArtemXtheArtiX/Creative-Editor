public static void saveItem(DataItem item, Minecraft minecraft) {
    if (item.getItem().getItem() != Items.AIR) {
        int slotId = item.getSlot().get();

        // Если слот не был установлен (0 по умолчанию) или это crafting слот (0-4),
        // используем выбранный слот хотбара
        if (slotId < 5 || slotId > 45) {
            slotId = 36 + minecraft.player.inventory.selected;
        }

        if (minecraft.hasSingleplayerServer()) {
            minecraft.getSingleplayerServer().getPlayerList().getPlayer(minecraft.player.getUUID()).inventoryMenu.setItem(slotId, item.getItemStack());
        } else {
            minecraft.getConnection().send(new CCreativeInventoryActionPacket(slotId, item.getItemStack()));
        }
    }
}