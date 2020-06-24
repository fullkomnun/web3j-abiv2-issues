pragma solidity >=0.4.21 <0.7.0;
pragma experimental ABIEncoderV2;

contract Journal {

    /////////////////////////////////////
    // DATA TYPES
    /////////////////////////////////////

    struct Note {
        string title;
        string content;
        uint insertionDate;
        address creator;
        address[] taggedMembers;
        string[] tags;
    }

    /////////////////////////////////////
    // STATE
    /////////////////////////////////////

    Note[] private notes;

    /////////////////////////////////////
    // EVENTS
    /////////////////////////////////////

    event NoteInserted(string indexed titleIndex, string title, string content, uint insertionDate, address creator, address[] taggedMembers, string[] tags);

    /////////////////////////////////////
    // TRANSACTIONS
    /////////////////////////////////////

    function insertNote(string memory _title, string memory _content, address[] memory _taggedMembers, string[] memory _tags) public {
        Note memory note = Note({
            title : _title,
            content : _content,
            insertionDate : now,
            creator : msg.sender,
            taggedMembers : _taggedMembers,
            tags : _tags
            });
        notes.push(note);
        emit NoteInserted(_title, _title, _content, note.insertionDate, note.creator, _taggedMembers, _tags);
    }
}
