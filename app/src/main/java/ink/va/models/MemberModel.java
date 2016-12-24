package ink.va.models;

/**
 * Created by USER on 2016-07-12.
 */
public class MemberModel {
    private String memberId;
    private String memberName;
    private String memberImage;
    private String memberItemId;
    private String memberGroupId;
    private boolean isFriend;
    private boolean isIncognito;


    public MemberModel(boolean isFriend, boolean isIncognito, String memberId, String memberName, String memberImage, String memberItemId, String memberGroupId) {
        this.memberId = memberId;
        this.isFriend = isFriend;
        this.isIncognito = isIncognito;
        this.memberName = memberName;
        this.memberImage = memberImage;
        this.memberItemId = memberItemId;
        this.memberGroupId = memberGroupId;
    }

    public boolean isIncognito() {
        return isIncognito;
    }

    public void setIncognito(boolean incognito) {
        isIncognito = incognito;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
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
