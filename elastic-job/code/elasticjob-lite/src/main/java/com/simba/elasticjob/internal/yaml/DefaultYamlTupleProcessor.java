package com.simba.elasticjob.internal.yaml;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 10:03
 * @Version V1.0
 **/
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Default YAML tuple processor.
 */
public final class DefaultYamlTupleProcessor {

    /**
     * Process node tuple.
     *
     * @param nodeTuple node tuple
     * @return processed node tuple
     */
    public NodeTuple process(final NodeTuple nodeTuple) {
        return isUnsetNodeTuple(nodeTuple.getValueNode()) ? null : nodeTuple;
    }

    private boolean isUnsetNodeTuple(final Node valueNode) {
        return isNullNode(valueNode) || isEmptyCollectionNode(valueNode);
    }

    private boolean isNullNode(final Node valueNode) {
        return Tag.NULL.equals(valueNode.getTag());
    }

    private boolean isEmptyCollectionNode(final Node valueNode) {
        return valueNode instanceof CollectionNode && (isEmptySequenceNode(valueNode) || isEmptyMappingNode(valueNode));
    }

    private boolean isEmptySequenceNode(final Node valueNode) {
        return Tag.SEQ.equals(valueNode.getTag()) && ((SequenceNode) valueNode).getValue().isEmpty();
    }

    private boolean isEmptyMappingNode(final Node valueNode) {
        return Tag.MAP.equals(valueNode.getTag()) && ((MappingNode) valueNode).getValue().isEmpty();
    }
}

