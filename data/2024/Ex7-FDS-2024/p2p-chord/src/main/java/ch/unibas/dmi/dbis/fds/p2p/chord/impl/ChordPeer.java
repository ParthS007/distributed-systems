package ch.unibas.dmi.dbis.fds.p2p.chord.impl;

import ch.unibas.dmi.dbis.fds.p2p.chord.api.*;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNetwork;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.Identifier;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircle;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircularInterval;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.math.CircularInterval;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircularInterval.createOpen;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class ChordPeer extends AbstractChordPeer {
  /**
   *
   * @param identifier
   * @param network
   */
  protected ChordPeer(Identifier identifier, ChordNetwork network) {
    super(identifier, network);
  }

  /**
   * Asks this {@link ChordNode} to find {@code id}'s successor {@link ChordNode}.
   *
   * Defined in [1], Figure 4
   *
   * @param caller The calling {@link ChordNode}. Used for simulation - not part of the actual chord definition.
   * @param id The {@link Identifier} for which to lookup the successor. Does not need to be the ID of an actual {@link ChordNode}!
   * @return The successor of the node {@code id} from this {@link ChordNode}'s point of view
   */
  @Override
  public ChordNode findSuccessor(ChordNode caller, Identifier id) {
    /* TODO: Implementation required. */
    ChordNode nprime;
    nprime = findPredecessor(this,id);
    return nprime.successor();
    //throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Asks this {@link ChordNode} to find {@code id}'s predecessor {@link ChordNode}
   *
   * Defined in [1], Figure 4
   *
   * @param caller The calling {@link ChordNode}. Used for simulation - not part of the actual chord definition.
   * @param id The {@link Identifier} for which to lookup the predecessor. Does not need to be the ID of an actual {@link ChordNode}!
   * @return The predecessor of or the node {@code of} from this {@link ChordNode}'s point of view
   */
  @Override
  public ChordNode findPredecessor(ChordNode caller, Identifier id) {
    /* TODO: Implementation required. */
    ChordNode nprime;
    nprime = this;
    CircularInterval<Integer> interval_t = CircularInterval.createLeftOpen(nprime.getIdentifier().getIndex(), nprime.successor().getIdentifier().getIndex());
    while(!interval_t.contains(id.getIndex())){
      nprime = nprime.closestPrecedingFinger(this,id);
      interval_t = CircularInterval.createLeftOpen(nprime.getIdentifier().getIndex(), nprime.successor().getIdentifier().getIndex());
    }
    return nprime;
    // throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Return the closest finger preceding the  {@code id}
   *
   * Defined in [1], Figure 4
   *
   * @param caller The calling {@link ChordNode}. Used for simulation - not part of the actual chord definition.
   * @param id The {@link Identifier} for which the closest preceding finger is looked up.
   * @return The closest preceding finger of the node {@code of} from this node's point of view
   */
  @Override
  public ChordNode closestPrecedingFinger(ChordNode caller, Identifier id) {
    /* TODO: Implementation required. */

    for(int i = this.getNetwork().getNbits(); i>=1; i--)
    {
      if(!this.fingerTable.node(i).isPresent()) continue;
      CircularInterval<Integer> interval_t = CircularInterval.createOpen(this.getIdentifier().getIndex(), id.getIndex());
      if(interval_t.contains(this.fingerTable.node(i).get().getIdentifier().getIndex())){
        return this.fingerTable.node(i).get();
      }

    }
    return this;
    // throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Called on this {@link ChordNode} if it wishes to join the {@link ChordNetwork}. {@code nprime} references another {@link ChordNode}
   * that is already member of the {@link ChordNetwork}.
   *
   * Required for static {@link ChordNetwork} mode. Since no stabilization takes place in this mode, the joining node must make all
   * the necessary setup.
   *
   * Defined in [1], Figure 6
   *
   * @param nprime Arbitrary {@link ChordNode} that is part of the {@link ChordNetwork} this {@link ChordNode} wishes to join.
   */
  @Override
  public void joinAndUpdate(ChordNode nprime) {
    if (nprime != null) {
      initFingerTable(nprime);
      updateOthers();
      ChordPeer successor = (ChordPeer) this.successor();
      Map<String, String> items = successor.dump();
      /* TODO: Move keys. */
      moveKeysBetweenNodes(items,this);
    } else {
      for (int i = 1; i <= getNetwork().getNbits(); i++) {
        this.fingerTable.setNode(i, this);
      }
      this.setPredecessor(this);
    }
  }


  public void moveKeysBetweenNodes(Map<String, String> items,ChordNode caller)
  {
    CircularInterval<Integer> interval = CircularInterval.createLeftOpen(caller.predecessor().getIdentifier().getIndex(), caller.getIdentifier().getIndex());
    for (Map.Entry<String, String> item : items.entrySet()) {
      int item_hash = this.getNetwork().getHashFunction().hash(item.getKey());
      if (interval.contains(getNetwork().getIdentifierCircle().getIdentifierAt(item_hash).getIndex())) {
        caller.store(caller, item.getKey(), item.getValue());
        caller.successor().delete(caller.successor(), item.getKey());
      }
    }

  }

  /**
   * Called on this {@link ChordNode} if i  t wishes to join the {@link ChordNetwork}. {@code nprime} references
   * another {@link ChordNode} that is already member of the {@link ChordNetwork}.
   *
   * Required for dynamic {@link ChordNetwork} mode. Since in that mode {@link ChordNode}s stabilize the network
   * periodically, this method simply sets its successor and waits for stabilization to do the rest.
   *
   * Defined in [1], Figure 7
   *
   * @param nprime Arbitrary {@link ChordNode} that is part of the {@link ChordNetwork} this {@link ChordNode} wishes to join.
   */
  @Override
  public void joinOnly(ChordNode nprime) {
    setPredecessor(null);
    if (nprime == null) {
      this.fingerTable.setNode(1, this);
    } else {
      this.fingerTable.setNode(1, nprime.findSuccessor(this,this));
    }
  }

  /**
   * Initializes this {@link ChordNode}'s {@link FingerTable} based on information derived from {@code nprime}.
   *
   * Defined in [1], Figure 6
   *
   * @param nprime Arbitrary {@link ChordNode} that is part of the network.
   */
  private void initFingerTable(ChordNode nprime) {
    CircularInterval<Integer> interval_t;
    this.fingerTable.setNode(1, nprime.findSuccessor(this, this.getNetwork().getIdentifierCircle().getIdentifierAt(this.fingerTable.start(1))));
    this.setPredecessor(this.successor().predecessor());
    this.successor().setPredecessor(this);
    for(int i = 1; i < getNetwork().getNbits(); i++ )
    {
      interval_t = CircularInterval.createClosed(this.getIdentifier().getIndex(), this.fingerTable.node(i).get().getIdentifier().getIndex());
      if(interval_t.contains(this.fingerTable.start(i+1))){
        this.fingerTable.setNode(i+1, this.fingerTable.node(i).get());

      }
      else{
        this.fingerTable.setNode(i+1, this.successor().findSuccessor(this, this.getNetwork().getIdentifierCircle().getIdentifierAt(this.fingerTable.start(i+1))));
      }

    }
    /* TODO: Implementation required. */
    //throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Updates all {@link ChordNode} whose {@link FingerTable} should refer to this {@link ChordNode}.
   *
   * Defined in [1], Figure 6
   */
  private void updateOthers() {
    ChordNode p;
    for(int i=1; i<= getNetwork().getNbits(); i++)
    {
      double d = this.getIdentifier().getIndex();
      p = this.findPredecessor(this, this.getNetwork().getIdentifierCircle().getIdentifierAt((int)(d + 1 - Math.pow(2,i-1) )));
      p.updateFingerTable(this, i);
    }
    /* TODO: Implementation required. */
    // throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * If node {@code s} is the i-th finger of this node, update this node's finger table with {@code s}
   *
   * Defined in [1], Figure 6
   *
   * @param s The should-be i-th finger of this node
   * @param i The index of {@code s} in this node's finger table
   */
  @Override
  public void updateFingerTable(ChordNode s, int i) {
    finger().node(i).ifPresent(node -> {
      ChordNode p;
      CircularInterval<Integer> interval_t;
      interval_t = CircularInterval.createLeftOpen(this.getIdentifier().getIndex(), this.fingerTable.node(i).get().getIdentifier().getIndex());
      if(interval_t.contains(s.getIdentifier().getIndex())){
        this.fingerTable.setNode(i,s);
        p = predecessor();
        p.updateFingerTable(s,i);
      }
//      /* TODO: Implementation required. */
//    //  throw new RuntimeException("This method has not been implemented!");
    });
  }

  /**
   * Called by {@code nprime} if it thinks it might be this {@link ChordNode}'s predecessor. Updates predecessor
   * pointers accordingly, if required.
   *
   * Defined in [1], Figure 7
   *
   * @param nprime The alleged predecessor of this {@link ChordNode}
   */
  @Override
  public void notify(ChordNode nprime) {
    if (this.status() == NodeStatus.OFFLINE || this.status() == NodeStatus.JOINING) return;
    CircularInterval<Integer> interval = CircularInterval.createOpen(this.predecessor().getIdentifier().getIndex(), this.getIdentifier().getIndex());
    if (interval.contains(nprime.getIdentifier().getIndex()) || this.predecessor() == null) {
      this.setPredecessor(nprime);
    }
    /* TODO: Implementation required. Hint: Null check on predecessor! */
//    throw new RuntimeException("This method has not been implemented!");

  }

  /**
   * Called periodically in order to refresh entries in this {@link ChordNode}'s {@link FingerTable}.
   *
   * Defined in [1], Figure 7
   */
  @Override
  public void fixFingers() {
    if (this.status() == NodeStatus.OFFLINE || this.status() == NodeStatus.JOINING) return;
    Random ran_int = new Random();
    int ran = ran_int.nextInt(getNetwork().getNbits()) + 1;
    this.fingerTable.setNode(ran, findSuccessor(this, this.getNetwork().getIdentifierCircle().getIdentifierAt(this.fingerTable.start(ran))));

    /* TODO: Implementation required */
//    throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Called periodically in order to verify this node's immediate successor and inform it about this
   * {@link ChordNode}'s presence,
   *
   * Defined in [1], Figure 7
   */
  @Override
  public void stabilize() {
    if (this.status() == NodeStatus.OFFLINE || this.status() == NodeStatus.JOINING || this.successor().predecessor() == null)return;
    ChordNode x;
    x = this.successor().predecessor();
    CircularInterval<Identifier> interval_t = CircularInterval.createOpen(this.getIdentifier(), this.successor().getIdentifier());
    if (interval_t.contains(x.getIdentifier())) {
      this.fingerTable.setNode(1,x);
    }
    this.successor().notify(this);
    /* TODO: Implementation required.*/
//    throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Called periodically in order to check activity of this {@link ChordNode}'s predecessor.
   *
   * Not part of [1]. Required for dynamic network to handle node failure.
   */
  @Override
  public void checkPredecessor() {
    if (this.status() == NodeStatus.OFFLINE || this.status() == NodeStatus.JOINING) return;

    if (this.predecessor() == null || this.predecessor().status() == NodeStatus.OFFLINE){
      this.setPredecessor(this.findPredecessor(this, this.getIdentifier()));
    }
    /* TODO: Implementation required. Hint: Null check on predecessor! */
//    throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Called periodically in order to check activity of this {@link ChordNode}'s successor.
   *
   * Not part of [1]. Required for dynamic network to handle node failure.
   */
  @Override
  public void checkSuccessor() {
    if (this.status() == NodeStatus.OFFLINE || this.status() == NodeStatus.JOINING) return;
    if (this.successor() == null || this.successor().status() == NodeStatus.OFFLINE){
      this.fingerTable.setNode(1, this.findSuccessor(this, this.getIdentifier()));
    }
    /* TODO: Implementation required. Hint: Null check on predecessor! */
//    throw new RuntimeException("This method has not been implemented!");
  }

  /**
   * Performs a lookup for where the data with the provided key should be stored.
   *
   * @return Node in which to store the data with the provided key.
   */
  @Override
  protected ChordNode lookupNodeForItem(String key) {
    /* TODO: Implementation required. Hint: Null check on predecessor! */
    ChordNode lookup_node;
    lookup_node = findSuccessor(this, this.getNetwork().getIdentifierCircle().getIdentifierAt(getNetwork().getHashFunction().hash(key)));
    return lookup_node;
//    throw new RuntimeException("This method has not been implemented!");
  }

  @Override
  public String toString() {
    return String.format("ChordPeer{id=%d}", this.id().getIndex());
  }
}
