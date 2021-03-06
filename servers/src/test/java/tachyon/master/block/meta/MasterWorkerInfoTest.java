/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.master.block.meta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import tachyon.Constants;
import tachyon.MasterStorageTierAssoc;
import tachyon.StorageTierAssoc;
import tachyon.thrift.WorkerInfo;
import tachyon.thrift.WorkerNetAddress;
import tachyon.worker.NetAddress;

/**
 * Unit tests for {@link MasterWorkerInfo}.
 */
public final class MasterWorkerInfoTest {
  private static final List<String> STORAGE_TIER_ALIASES = Lists.newArrayList("MEM", "SSD");
  private static final StorageTierAssoc GLOBAL_STORAGE_TIER_ASSOC = new MasterStorageTierAssoc(
      STORAGE_TIER_ALIASES);
  private static final Map<String, Long> TOTAL_BYTES_ON_TIERS =
      ImmutableMap.of("MEM", Constants.KB * 3L, "SSD", Constants.KB * 3L);
  private static final Map<String, Long> USED_BYTES_ON_TIERS =
      ImmutableMap.of("MEM", Constants.KB * 1L, "SSD", Constants.KB * 1L);
  private static final Set<Long> NEW_BLOCKS = Sets.newHashSet(1L, 2L);
  private MasterWorkerInfo mInfo;

  @Rule
  public ExpectedException mThrown = ExpectedException.none();

  @Before
  public void before() {
    // register
    mInfo = new MasterWorkerInfo(0, new NetAddress(new WorkerNetAddress()));
    mInfo.register(GLOBAL_STORAGE_TIER_ASSOC, STORAGE_TIER_ALIASES, TOTAL_BYTES_ON_TIERS,
        USED_BYTES_ON_TIERS, NEW_BLOCKS);
  }

  @Test
  public void registerTest() {
    Assert.assertEquals(NEW_BLOCKS, mInfo.getBlocks());
    Assert.assertEquals(TOTAL_BYTES_ON_TIERS, mInfo.getTotalBytesOnTiers());
    Assert.assertEquals(Constants.KB * 6L, mInfo.getCapacityBytes());
    Assert.assertEquals(USED_BYTES_ON_TIERS, mInfo.getUsedBytesOnTiers());
    Assert.assertEquals(Constants.KB * 2L, mInfo.getUsedBytes());
  }

  @Test
  public void getFreeBytesOnTiersTest() {
    Assert.assertEquals(ImmutableMap.of("MEM", Constants.KB * 2L, "SSD", Constants.KB * 2L),
        mInfo.getFreeBytesOnTiers());
  }

  @Test
  public void registerAgainTest() {
    Set<Long> newBlocks = Sets.newHashSet(3L);
    Set<Long> removedBlocks = mInfo.register(GLOBAL_STORAGE_TIER_ASSOC, STORAGE_TIER_ALIASES,
        TOTAL_BYTES_ON_TIERS, USED_BYTES_ON_TIERS, newBlocks);
    Assert.assertEquals(NEW_BLOCKS, removedBlocks);
    Assert.assertEquals(newBlocks, mInfo.getBlocks());
  }

  @Test
  public void registerWithDifferentNumberOfTiersTest() {
    mThrown.expect(IllegalArgumentException.class);
    mThrown.expectMessage("totalBytesOnTiers and usedBytesOnTiers should have the same number of"
        + " tiers as storageTierAliases, but storageTierAliases has 2 tiers, while"
        + " totalBytesOnTiers has 2 tiers and usedBytesOnTiers has 1 tiers");

    mInfo.register(GLOBAL_STORAGE_TIER_ASSOC, STORAGE_TIER_ALIASES, TOTAL_BYTES_ON_TIERS,
        ImmutableMap.of("SSD", Constants.KB * 1L), NEW_BLOCKS);
  }

  @Test
  public void blockOperationTest() {
    // add existing block
    mInfo.addBlock(1L);
    Assert.assertEquals(NEW_BLOCKS, mInfo.getBlocks());
    // add a new block
    mInfo.addBlock(3L);
    Assert.assertTrue(mInfo.getBlocks().contains(3L));
    // remove block
    mInfo.removeBlock(3L);
    Assert.assertFalse(mInfo.getBlocks().contains(3L));
  }

  @Test
  public void workerInfoGenerationTest() {
    WorkerInfo workerInfo = mInfo.generateClientWorkerInfo();
    Assert.assertEquals(mInfo.getId(), workerInfo.id);
    Assert.assertEquals(mInfo.getWorkerAddress().toThrift(), workerInfo.address);
    Assert.assertEquals("In Service", workerInfo.state);
    Assert.assertEquals(mInfo.getCapacityBytes(), workerInfo.capacityBytes);
    Assert.assertEquals(mInfo.getUsedBytes(), workerInfo.usedBytes);
    Assert.assertEquals(mInfo.getStartTime(), workerInfo.startTimeMs);
  }

  @Test
  public void updateToRemovedBlockTest() {
    // remove a non-existing block
    mInfo.updateToRemovedBlock(true, 10L);
    Assert.assertTrue(mInfo.getToRemoveBlocks().isEmpty());
    // remove block 1
    mInfo.updateToRemovedBlock(true, 1L);
    Assert.assertTrue(mInfo.getToRemoveBlocks().contains(1L));
    // cancel the removal
    mInfo.updateToRemovedBlock(false, 1L);
    Assert.assertTrue(mInfo.getToRemoveBlocks().isEmpty());
    // actually remove 1 for real
    mInfo.updateToRemovedBlock(true, 1L);
    mInfo.removeBlock(1L);
    Assert.assertTrue(mInfo.getToRemoveBlocks().isEmpty());
  }

  @Test
  public void updateUsedBytesTest() {
    Assert.assertEquals(Constants.KB * 2L, mInfo.getUsedBytes());
    Map<String, Long> usedBytesOnTiers =
        ImmutableMap.of("MEM", Constants.KB * 2L, "SSD", Constants.KB * 1L);
    mInfo.updateUsedBytes(usedBytesOnTiers);
    Assert.assertEquals(usedBytesOnTiers, mInfo.getUsedBytesOnTiers());
    Assert.assertEquals(Constants.KB * 3L, mInfo.getUsedBytes());
  }

  @Test
  public void updateUsedBytesInTierTest() {
    Assert.assertEquals(Constants.KB * 2L, mInfo.getUsedBytes());
    mInfo.updateUsedBytes("MEM", Constants.KB * 2L);
    Assert.assertEquals(Constants.KB * 3L, mInfo.getUsedBytes());
    Assert.assertEquals(Constants.KB * 2L, (long) mInfo.getUsedBytesOnTiers().get("MEM"));
  }
}
