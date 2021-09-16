package org.sunbird.workallocation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class CompetencyDetails {
    private String type;
    private String id;
    private String name;
    private String description;
    private String source;
    private String status;
    private String level;
    private AdditionalProperties additionalProperties;
    private List<Child> children;

    public String getType() {
        return type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AdditionalProperties getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(AdditionalProperties additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public FracRequest getFracRequest(String source, Child child) {
        FracRequest req = new FracRequest();
        req.setSource(source);
        req.setName(name);
        req.setType("COMPETENCY");
        req.setDescription(description);
        if ("".equals(id)) {
            req.setId(null);
        } else {
            req.setId(id);
        }
        req.setAdditionalProperties(additionalProperties);

        List<ChildNode> childNodeList = new ArrayList<>();
        if (child != null) {
            ChildNode newCN = new ChildNode();
            newCN.setName(child.getName());
            newCN.setType(child.getType());
            newCN.setSource(source);
            newCN.setLevel(child.getLevel());
            childNodeList.add(newCN);
            req.setChildren(childNodeList);
        } else {
            if (!CollectionUtils.isEmpty(children)) {
                for (Child cn : children) {
                    ChildNode newCN = new ChildNode();
                    if (!StringUtils.isEmpty(cn.getId()))
                        newCN.setId(cn.getId());
                    newCN.setName(cn.getName());
                    newCN.setType(cn.getType());
                    newCN.setSource(source);
                    newCN.setDescription(cn.getDescription());
                    newCN.setLevel(cn.getLevel());
                    childNodeList.add(newCN);
                    req.setChildren(childNodeList);
                }
            }
        }

//				if (req.getChildren() != null && req.getChildren().size() < 5) {
//					Set<String> levelSet = children.stream().map(Child::getLevel).collect(Collectors.toSet());
//					// We need to add more children
//					for (int i = 1; i <= 5; i++) {
//						String level = "Level "  + i;
//						if(levelSet.contains(level)) {
//							continue;
//						}
//						ChildNode newCN = new ChildNode();
//						newCN.setLevel(level);
//						newCN.setName(level);
//						newCN.setType("COMPETENCIESLEVEL");
//						newCN.setSource(source);
//						childNodeList.add(newCN);
//					}
//				}

        req.setChildren(childNodeList);

        return req;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDetails that = (CompetencyDetails) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
