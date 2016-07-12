package ink.models;

/**
 * Created by USER on 2016-07-12.
 */
public class MemberModel {
    private String memberId;
    private String memberName;
    private String memberImage;
    private String memberItemId;
    private String memberGroupId;


    public MemberModel(String memberId, String memberName, String memberImage, String memberItemId, String memberGroupId) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberImage = memberImage;
        this.memberItemId = memberItemId;
        this.memberGroupId = memberGroupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberImage() {
        return memberImage;
    }

    public void setMemberImage(String memberImage) {
        this.memberImage = memberImage;
    }

    public String getMemberItemId() {
        return memberItemId;
    }

    public void setMemberItemId(String memberItemId) {
        this.memberItemId = memberItemId;
    }

    public String getMemberGroupId() {
        return memberGroupId;
    }

    public void setMemberGroupId(String memberGroupId) {
        this.memberGroupId = memberGroupId;
    }
}
