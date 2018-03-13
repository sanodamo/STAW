USE [JAC]
GO

/****** Object:  Table [dbo].[MASTER_TABLE]    Script Date: 3/11/2018 9:16:13 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[MASTER_TABLE](
	[ID] [bigint] IDENTITY(1,1) NOT NULL,
	[P_ID] [varchar](255) NOT NULL,
	[KEY_VAL] [varchar](255) NOT NULL,
	[VAL] [varchar](2000) NOT NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO
