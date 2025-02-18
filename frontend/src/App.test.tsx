import {test, expect, vi, afterEach, describe, beforeEach} from "vitest";
import {cleanup, render, screen, waitFor} from "@testing-library/react";
import App from "./App.tsx";
import axios from "axios";
import {userEvent} from "@testing-library/user-event";

describe("App", () => {

  beforeEach(()=>{
    vi.spyOn(axios, "get").mockResolvedValue({data: []})
  })

  afterEach(() => {
    cleanup()
  })

  test("MAINページを開く", () => {
    render(<App/>)
    expect(screen.getByText("Todo List")).not.toBeNull()
  })

  test("given getTodos has items when render app then see todo item", async () => {
    const spyGet = vi.spyOn(axios, "get").mockResolvedValue(
      {
        data: [
          {pk: "1000001", text: "Hello World"}
        ]
      }
    )
    render(<App/>)

    await waitFor(()=>{
      expect(screen.getByText(/Hello World/)).not.toBeNull()
      expect(spyGet).toHaveBeenCalledWith("/todo")
    })
  })

  test("write todo item and click submit then send POST request to server", async() => {

    const spyPost = vi.spyOn(axios, "post").mockResolvedValue(undefined)
    render(<App/>)

    await userEvent.type(screen.getByRole("textbox"), "Hello World")
    await userEvent.click(screen.getByRole("button", {name: "追加"}))

    expect(spyPost).toHaveBeenCalledWith("/todo", {text: "Hello World"})
    expect(screen.getByRole("textbox").getAttribute("value")).toEqual("")
  })

  test("write todo item and click submit then see new todo data", async() => {
    const spyGet = vi.spyOn(axios, "get")
      .mockResolvedValueOnce({data: []})
      .mockResolvedValue({data: [{pk: "1000001", text: "Hello World"}]})

    vi.spyOn(axios, "post").mockResolvedValue(undefined)
    render(<App/>)

    await userEvent.type(screen.getByRole("textbox"), "Hello World")
    await userEvent.click(screen.getByRole("button", {name: "追加"}))

    await waitFor(() => {
      expect(spyGet).toHaveBeenNthCalledWith(2,"/todo")
      expect(screen.getByText(/Hello World/)).not.toBeNull()
    })
  })

  // test("click delete then data disappears", () => {
  //   const spyGet = vi.spyOn(axios, "get")
  //     .mockResolvedValueOnce({data: [{pk:"12345", text: "Hello"}]})
  //     .mockResolvedValueOnce({data: []})
  //
  //   const spyDelete = vi.spyOn(axios,"delete").mockResolvedValue(undefined)
  //   render(<App />)
  // })
})

