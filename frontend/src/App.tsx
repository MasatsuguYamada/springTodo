import './App.css'
import {useEffect,useState} from "react";
import axios from "axios";

interface TodoItem {
  pk: string;
  text: string;
}

function App() {
const [allItems, setAllItems] = useState<TodoItem[]>([])
const [text, setText] = useState("")
  useEffect(() => {
    axios.get("/todo")
      .then(response => {
        console.log(response.data)
        setAllItems(response.data)
      })
  }, []);

  return (
    <>
      MAIN PAGE
      {allItems.map((todoItem, index) => {
        return (
          <>
            <div key={index}>
              <h2>{index +1}番目のデータ</h2>
              {/*<div>{todoItem.PK}</div>*/}
              <div>{todoItem.text}</div>
            </div>
          </>
        )
      })}
      <div>
        <input type="text" value={text} onChange={(e) => setText(e.target.value)}/>
        <button onClick={() => {
          axios.post("/todo", {text: text})
            .then(()=>{
              axios.get("/todo")
                .then(response => {
              console.log(response.data)
              setAllItems(response.data)
            })})
          setText("")
        }}>Submit</button>
      </div>
      <div>
        <button onClick={()=>{}}>
          delete
        </button>
      </div>
    </>
  )
}

export default App
