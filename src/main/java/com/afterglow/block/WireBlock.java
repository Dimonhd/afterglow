package com.afterglow.block;

import com.afterglow.grid.CircuitManager;
import com.afterglow.grid.ElectricBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/** Провод: узел цепи, соединяется с соседними проводами и с выводами компонентов. */
public class WireBlock extends Block implements ElectricBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final Map<Direction, BooleanProperty> PROPS = new EnumMap<>(Direction.class);
    static {
        PROPS.put(Direction.NORTH, NORTH);
        PROPS.put(Direction.EAST, EAST);
        PROPS.put(Direction.SOUTH, SOUTH);
        PROPS.put(Direction.WEST, WEST);
        PROPS.put(Direction.UP, UP);
        PROPS.put(Direction.DOWN, DOWN);
    }

    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    static {
        ARMS.put(Direction.DOWN, Block.box(6, 0, 6, 10, 6, 10));
        ARMS.put(Direction.UP, Block.box(6, 10, 6, 10, 16, 10));
        ARMS.put(Direction.NORTH, Block.box(6, 6, 0, 10, 10, 6));
        ARMS.put(Direction.SOUTH, Block.box(6, 6, 10, 10, 10, 16));
        ARMS.put(Direction.WEST, Block.box(0, 6, 6, 6, 10, 10));
        ARMS.put(Direction.EAST, Block.box(10, 6, 6, 16, 10, 10));
    }

    public WireBlock(Properties properties) {
        super(properties);
        BlockState s = stateDefinition.any();
        for (BooleanProperty p : PROPS.values()) s = s.setValue(p, false);
        registerDefaultState(s);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    private static boolean connectsTo(BlockState neighbor, Direction dir) {
        return neighbor.getBlock() instanceof ElectricBlock eb && eb.hasTerminal(neighbor, dir.getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockGetter level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState s = defaultBlockState();
        for (Direction d : Direction.values()) {
            s = s.setValue(PROPS.get(d), connectsTo(level.getBlockState(pos.relative(d)), d));
        }
        return s;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPS.get(dir), connectsTo(neighborState, dir));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = CORE;
        for (Direction d : Direction.values()) {
            if (state.getValue(PROPS.get(d))) shape = Shapes.or(shape, ARMS.get(d));
        }
        return shape;
    }

    @Override
    public boolean hasTerminal(BlockState state, Direction face) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) CircuitManager.markDirty(level);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) CircuitManager.markDirty(level);
    }
}
